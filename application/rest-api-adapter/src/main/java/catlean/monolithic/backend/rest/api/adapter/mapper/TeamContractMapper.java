package catlean.monolithic.backend.rest.api.adapter.mapper;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Team;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import fr.catlean.monolithic.backend.frontend.contract.api.model.CreateTeamRequestContract;
import fr.catlean.monolithic.backend.frontend.contract.api.model.CreateTeamResponseContract;
import fr.catlean.monolithic.backend.frontend.contract.api.model.CreateTeamResponseContractTeam;
import fr.catlean.monolithic.backend.frontend.contract.api.model.PostCreateTeamsResponseContract;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface TeamContractMapper {

    static CreateTeamResponseContract domainToCreateTeamResponseContract(final Team team) {
        final CreateTeamResponseContract createTeamResponseContract = new CreateTeamResponseContract();
        final CreateTeamResponseContractTeam createTeamResponseContractTeam = new CreateTeamResponseContractTeam();
        createTeamResponseContractTeam.setId(team.getId());
        createTeamResponseContractTeam.setName(team.getName());
        createTeamResponseContractTeam.setRepositoryIds(team.getRepositories().stream().map(Repository::getId).toList());
        createTeamResponseContract.setTeam(createTeamResponseContractTeam);
        return createTeamResponseContract;
    }

    static Map<String, List<Long>> getRepositoryIdsMappedToTeamName(List<CreateTeamRequestContract> createTeamRequestContract) {
        final Map<String, List<Long>> repositoryIdsMappedToTeamName = new HashMap<>();
        for (CreateTeamRequestContract teamRequestContract : createTeamRequestContract) {
            repositoryIdsMappedToTeamName.put(teamRequestContract.getName(), teamRequestContract.getRepositoryIds());
        }
        return repositoryIdsMappedToTeamName;
    }

    static PostCreateTeamsResponseContract getPostCreateTeamsResponseContract(List<Team> teamsForNameAndRepositoriesAndUser) {
        final List<CreateTeamResponseContract> createTeamResponseContracts =
                teamsForNameAndRepositoriesAndUser
                        .stream()
                        .map(TeamContractMapper::domainToCreateTeamResponseContract)
                        .toList();
        final PostCreateTeamsResponseContract postCreateTeamsResponseContract =
                new PostCreateTeamsResponseContract();
        postCreateTeamsResponseContract.setTeams(createTeamResponseContracts);
        return postCreateTeamsResponseContract;
    }

    static PostCreateTeamsResponseContract getPostCreateTeamsResponseContractError(CatleanException e) {
        final PostCreateTeamsResponseContract postCreateTeamsResponseContract =
                new PostCreateTeamsResponseContract();
        postCreateTeamsResponseContract.setErrors(List.of(CatleanErrorContractMapper.catleanExceptionToContract(e)));
        return postCreateTeamsResponseContract;
    }
}
