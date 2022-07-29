package catlean.monolithic.backend.rest.api.adapter;

import catlean.monolithic.backend.rest.api.adapter.authentication.AuthenticationService;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Team;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.port.in.TeamFacadeAdapter;
import fr.catlean.monolithic.backend.frontend.contract.api.TeamApi;
import fr.catlean.monolithic.backend.frontend.contract.api.model.CatleanErrorsContract;
import fr.catlean.monolithic.backend.frontend.contract.api.model.CreateTeamRequestContract;
import fr.catlean.monolithic.backend.frontend.contract.api.model.TeamsResponseContract;
import fr.catlean.monolithic.backend.frontend.contract.api.model.UpdateTeamRequestContract;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static catlean.monolithic.backend.rest.api.adapter.mapper.CatleanErrorContractMapper.catleanExceptionToContracts;
import static catlean.monolithic.backend.rest.api.adapter.mapper.TeamContractMapper.*;
import static org.springframework.http.ResponseEntity.internalServerError;
import static org.springframework.http.ResponseEntity.ok;

@AllArgsConstructor
@RestController
@Tags(@Tag(name = "Team"))
public class TeamRestApiAdapter implements TeamApi {

    private final AuthenticationService authenticationService;
    private final TeamFacadeAdapter teamFacadeAdapter;

    @Override
    public ResponseEntity<TeamsResponseContract> createTeam(List<CreateTeamRequestContract> createTeamRequestContract) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();

            final Map<String, List<String>> repositoryIdsMappedToTeamName =
                    getRepositoryIdsMappedToTeamName(createTeamRequestContract);
            final List<Team> teamsForNameAndRepositoriesAndUser =
                    teamFacadeAdapter.createTeamsForNameAndRepositoriesAndUser(repositoryIdsMappedToTeamName,
                            authenticatedUser);
            final TeamsResponseContract postCreateTeamsResponseContract =
                    getTeamsResponseContract(teamsForNameAndRepositoriesAndUser);
            return ok(postCreateTeamsResponseContract);
        } catch (CatleanException e) {
            final TeamsResponseContract postCreateTeamsResponseContract =
                    getTeamsResponseContractError(e);
            return internalServerError().body(postCreateTeamsResponseContract);
        }
    }

    @Override
    public ResponseEntity<TeamsResponseContract> getTeams() {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            final TeamsResponseContract postCreateTeamsResponseContract =
                    getTeamsResponseContract(teamFacadeAdapter.getTeamsForOrganization(authenticatedUser.getOrganization()));
            return ok(postCreateTeamsResponseContract);
        } catch (CatleanException e) {
            final TeamsResponseContract postCreateTeamsResponseContract =
                    getTeamsResponseContractError(e);
            return internalServerError().body(postCreateTeamsResponseContract);
        }
    }

    @Override
    public ResponseEntity<CatleanErrorsContract> deleteTeam(final UUID teamId) {
        try {
            teamFacadeAdapter.deleteForId(teamId);
            return ok().build();
        } catch (CatleanException e) {
            return internalServerError().body(catleanExceptionToContracts(e));
        }
    }

    @Override
    public ResponseEntity<CatleanErrorsContract> updateTeam(UpdateTeamRequestContract updateTeamRequestContract) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            final Team team = getTeamToPatch(updateTeamRequestContract)
                    .toBuilder()
                    .organizationId(authenticatedUser.getId())
                    .build();
            teamFacadeAdapter.update(team);
            return ok().build();
        } catch (CatleanException e) {
            return internalServerError().body(catleanExceptionToContracts(e));
        }
    }
}
