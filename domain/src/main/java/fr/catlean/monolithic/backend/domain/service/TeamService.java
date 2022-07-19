package fr.catlean.monolithic.backend.domain.service;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.Repository;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.Team;
import fr.catlean.monolithic.backend.domain.port.out.AccountTeamStorage;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class TeamService {

    private final AccountTeamStorage accountTeamStorage;

    public Team createTeamForNameAndRepositoriesAndOrganization(String teamName, List<Integer> repositoryIds,
                                                                Organization organization) throws CatleanException {
        return accountTeamStorage.createTeam(
                Team.builder()
                        .name(teamName)
                        .repositories(repositoryIds.stream().map(id -> Repository.builder().id(id).build()).toList())
                        .organizationId(organization.getId())
                        .build()
        );
    }
}
