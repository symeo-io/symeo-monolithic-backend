package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.domain.bff.model.account.Team;
import io.symeo.monolithic.backend.domain.bff.model.vcs.RepositoryView;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.frontend.contract.api.model.CreateTeamRequestContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.CreateTeamResponseContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.TeamsResponseContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.UpdateTeamRequestContract;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface TeamContractMapper {

    static CreateTeamResponseContract domainToCreateTeamResponseContract(final Team team) {
        final CreateTeamResponseContract createTeamResponseContract = new CreateTeamResponseContract();
        createTeamResponseContract.setId(team.getId());
        createTeamResponseContract.setName(team.getName());
        createTeamResponseContract.setRepositoryIds(team.getRepositories().stream().map(RepositoryView::getId).toList());
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

    static TeamsResponseContract getTeamsResponseContractError(SymeoException e) {
        final TeamsResponseContract postCreateTeamsResponseContract =
                new TeamsResponseContract();
        postCreateTeamsResponseContract.setErrors(List.of(SymeoErrorContractMapper.exceptionToContract(e)));
        return postCreateTeamsResponseContract;
    }

    static Team getTeamToPatch(UpdateTeamRequestContract updateTeamRequestContract) {
        return Team.builder()
                .id(updateTeamRequestContract.getId())
                .name(updateTeamRequestContract.getName())
                .repositories(updateTeamRequestContract.getRepositoryIds().stream().map(s -> RepositoryView.builder().id(s).build()).toList())
                .build();
    }
}
