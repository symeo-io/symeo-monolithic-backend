package catlean.monolithic.backend.rest.api.adapter.mapper;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.Repository;
import fr.catlean.monolithic.backend.domain.model.account.Team;
import fr.catlean.monolithic.backend.frontend.contract.api.model.CreateTeamResponseContract;
import fr.catlean.monolithic.backend.frontend.contract.api.model.CreateTeamResponseContractTeam;

import java.util.List;

public interface TeamResponseMapper {

    static CreateTeamResponseContract domainToCreateTeamResponseContract(final Team team) {
        final CreateTeamResponseContract createTeamResponseContract = new CreateTeamResponseContract();
        final CreateTeamResponseContractTeam createTeamResponseContractTeam = new CreateTeamResponseContractTeam();
        createTeamResponseContractTeam.setId(team.getId());
        createTeamResponseContractTeam.setName(team.getName());
        createTeamResponseContractTeam.setRepositoryIds(team.getRepositories().stream().map(Repository::getId).toList());
        createTeamResponseContract.setTeam(createTeamResponseContractTeam);
        return createTeamResponseContract;
    }

    static CreateTeamResponseContract errorToContract(final CatleanException catleanException) {
        final CreateTeamResponseContract createTeamResponseContract = new CreateTeamResponseContract();
        createTeamResponseContract.setErrors(
                List.of(CatleanErrorResponseMapper.catleanExceptionToContract(catleanException))
        );
        return createTeamResponseContract;
    }
}
