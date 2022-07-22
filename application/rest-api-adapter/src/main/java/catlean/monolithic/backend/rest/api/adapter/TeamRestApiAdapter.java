package catlean.monolithic.backend.rest.api.adapter;

import catlean.monolithic.backend.rest.api.adapter.authentication.AuthenticationService;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Team;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.port.in.TeamFacadeAdapter;
import fr.catlean.monolithic.backend.frontend.contract.api.TeamApi;
import fr.catlean.monolithic.backend.frontend.contract.api.model.CreateTeamRequestContract;
import fr.catlean.monolithic.backend.frontend.contract.api.model.PostCreateTeamsResponseContract;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static catlean.monolithic.backend.rest.api.adapter.mapper.TeamContractMapper.*;

@AllArgsConstructor
@RestController
@Tags(@Tag(name = "Team"))
public class TeamRestApiAdapter implements TeamApi {

    private final AuthenticationService authenticationService;
    private final TeamFacadeAdapter teamFacadeAdapter;

    @Override
    public ResponseEntity<PostCreateTeamsResponseContract> createTeam(List<CreateTeamRequestContract> createTeamRequestContract) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();

            final Map<String, List<Long>> repositoryIdsMappedToTeamName =
                    getRepositoryIdsMappedToTeamName(createTeamRequestContract);
            final List<Team> teamsForNameAndRepositoriesAndUser =
                    teamFacadeAdapter.createTeamsForNameAndRepositoriesAndUser(repositoryIdsMappedToTeamName,
                            authenticatedUser);
            final PostCreateTeamsResponseContract postCreateTeamsResponseContract =
                    getPostCreateTeamsResponseContract(teamsForNameAndRepositoriesAndUser);
            return ResponseEntity.ok(postCreateTeamsResponseContract);
        } catch (CatleanException e) {
            final PostCreateTeamsResponseContract postCreateTeamsResponseContract =
                    getPostCreateTeamsResponseContractError(e);
            return ResponseEntity.internalServerError().body(postCreateTeamsResponseContract);
        }
    }

    @Override
    public ResponseEntity<PostCreateTeamsResponseContract> getTeams() {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            final PostCreateTeamsResponseContract postCreateTeamsResponseContract =
                    getPostCreateTeamsResponseContract(teamFacadeAdapter.getTeamsForOrganization(authenticatedUser.getOrganization()));
            return ResponseEntity.ok(postCreateTeamsResponseContract);
        } catch (CatleanException e) {
            final PostCreateTeamsResponseContract postCreateTeamsResponseContract =
                    getPostCreateTeamsResponseContractError(e);
            return ResponseEntity.internalServerError().body(postCreateTeamsResponseContract);
        }
    }
}
