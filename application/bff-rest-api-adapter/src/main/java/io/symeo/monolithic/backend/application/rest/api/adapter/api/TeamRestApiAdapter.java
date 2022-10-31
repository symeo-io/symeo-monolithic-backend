package io.symeo.monolithic.backend.application.rest.api.adapter.api;

import io.symeo.monolithic.backend.application.rest.api.adapter.authentication.AuthenticationService;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.bff.model.account.Team;
import io.symeo.monolithic.backend.domain.bff.model.account.User;
import io.symeo.monolithic.backend.domain.bff.port.in.TeamFacadeAdapter;
import io.symeo.monolithic.backend.bff.contract.api.TeamApi;
import io.symeo.monolithic.backend.bff.contract.api.model.SymeoErrorsContract;
import io.symeo.monolithic.backend.bff.contract.api.model.CreateTeamRequestContract;
import io.symeo.monolithic.backend.bff.contract.api.model.TeamsResponseContract;
import io.symeo.monolithic.backend.bff.contract.api.model.UpdateTeamRequestContract;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.SymeoErrorContractMapper.*;
import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.SymeoErrorContractMapper.mapSymeoExceptionToContract;
import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.TeamContractMapper.*;
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
        } catch (SymeoException e) {
            return mapSymeoExceptionToContract(() -> getTeamsResponseContractError(e), e);
        }
    }

    @Override
    public ResponseEntity<TeamsResponseContract> getTeams() {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            final TeamsResponseContract postCreateTeamsResponseContract =
                    getTeamsResponseContract(teamFacadeAdapter.getTeamsForOrganization(authenticatedUser.getOrganization()));
            return ok(postCreateTeamsResponseContract);
        } catch (SymeoException e) {
            return mapSymeoExceptionToContract(() -> getTeamsResponseContractError(e), e);
        }
    }

    @Override
    public ResponseEntity<SymeoErrorsContract> deleteTeam(final UUID teamId) {
        try {
            teamFacadeAdapter.deleteForId(teamId);
            return ok().build();
        } catch (SymeoException e) {
            return mapSymeoExceptionToContract(() -> exceptionToContracts(e), e);
        }
    }

    @Override
    public ResponseEntity<SymeoErrorsContract> updateTeam(UpdateTeamRequestContract updateTeamRequestContract) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            final Team team = getTeamToPatch(updateTeamRequestContract)
                    .toBuilder()
                    .organizationId(authenticatedUser.getId())
                    .build();
            teamFacadeAdapter.update(team);
            return ok().build();
        } catch (SymeoException e) {
            return mapSymeoExceptionToContract(() -> exceptionToContracts(e), e);
        }
    }
}
