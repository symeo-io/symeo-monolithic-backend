package catlean.monolithic.backend.rest.api.adapter.mapper;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Team;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import fr.catlean.monolithic.backend.frontend.contract.api.model.CreateTeamRequestContract;
import fr.catlean.monolithic.backend.frontend.contract.api.model.CreateTeamResponseContract;
import fr.catlean.monolithic.backend.frontend.contract.api.model.TeamsResponseContract;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface TeamContractMapper {

    static CreateTeamResponseContract domainToCreateTeamResponseContract(final Team team) {
        final CreateTeamResponseContract createTeamResponseContract = new CreateTeamResponseContract();
        createTeamResponseContract.setId(team.getId());
        createTeamResponseContract.setName(team.getName());
        createTeamResponseContract.setRepositoryIds(team.getRepositories().stream().map(Repository::getId).toList());
        return createTeamResponseContract;
    }

    static Map<String, List<String>> getRepositoryIdsMappedToTeamName(List<CreateTeamRequestContract> createTeamRequestContract) {
        final Map<String, List<String>> repositoryIdsMappedToTeamName = new HashMap<>();
        for (CreateTeamRequestContract teamRequestContract : createTeamRequestContract) {
            repositoryIdsMappedToTeamName.put(teamRequestContract.getName(), teamRequestContract.getRepositoryIds());
        }
        return repositoryIdsMappedToTeamName;
    }

    static TeamsResponseContract getTeamsResponseContract(List<Team> teamsForNameAndRepositoriesAndUser) {
        final List<CreateTeamResponseContract> createTeamResponseContracts =
                teamsForNameAndRepositoriesAndUser
                        .stream()
                        .map(TeamContractMapper::domainToCreateTeamResponseContract)
                        .toList();
        final TeamsResponseContract postCreateTeamsResponseContract =
                new TeamsResponseContract();
        postCreateTeamsResponseContract.setTeams(createTeamResponseContracts);
        return postCreateTeamsResponseContract;
    }

    static TeamsResponseContract getTeamsResponseContractError(CatleanException e) {
        final TeamsResponseContract postCreateTeamsResponseContract =
                new TeamsResponseContract();
        postCreateTeamsResponseContract.setErrors(List.of(CatleanErrorContractMapper.catleanExceptionToContract(e)));
        return postCreateTeamsResponseContract;
    }
}
