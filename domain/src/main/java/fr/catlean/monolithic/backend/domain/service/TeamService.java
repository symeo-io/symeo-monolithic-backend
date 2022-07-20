package fr.catlean.monolithic.backend.domain.service;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.Repository;
import fr.catlean.monolithic.backend.domain.model.account.Team;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.port.out.AccountTeamStorage;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class TeamService {

    private final AccountTeamStorage accountTeamStorage;

    public Team createTeamForNameAndRepositoriesAndUser(String teamName, List<Integer> repositoryIds,
                                                        User user) throws CatleanException {
        user.hasConfiguredTeam();
        return accountTeamStorage.createTeamForUser(
                Team.builder()
                        .name(teamName)
                        .repositories(repositoryIds.stream().map(id -> Repository.builder().id(id).build()).toList())
                        .organizationId(user.getOrganization().getId())
                        .build(),
                user
        );
    }
}
