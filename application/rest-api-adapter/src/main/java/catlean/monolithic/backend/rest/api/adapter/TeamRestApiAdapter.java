package catlean.monolithic.backend.rest.api.adapter;

import catlean.monolithic.backend.rest.api.adapter.authentication.AuthenticationService;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.service.TeamService;
import fr.catlean.monolithic.backend.frontend.contract.api.TeamApi;
import fr.catlean.monolithic.backend.frontend.contract.api.model.CreateTeamRequestContract;
import fr.catlean.monolithic.backend.frontend.contract.api.model.CreateTeamResponseContract;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static catlean.monolithic.backend.rest.api.adapter.mapper.TeamResponseMapper.domainToCreateTeamResponseContract;
import static catlean.monolithic.backend.rest.api.adapter.mapper.TeamResponseMapper.errorToContract;
import static org.springframework.http.ResponseEntity.ok;

@AllArgsConstructor
@RestController
@Tags(@Tag(name = "Team"))
public class TeamRestApiAdapter implements TeamApi {

    private final AuthenticationService authenticationService;
    private final TeamService teamService;

    @Override
    public ResponseEntity<CreateTeamResponseContract> createTeam(CreateTeamRequestContract createTeamRequestContract) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(domainToCreateTeamResponseContract(
                    teamService.createTeamForNameAndRepositoriesAndUser(createTeamRequestContract.getName(),
                            createTeamRequestContract.getRepositoryIds(),
                            authenticatedUser))
            );
        } catch (CatleanException e) {
            return ResponseEntity.internalServerError().body(errorToContract(e));
        }
    }
}
