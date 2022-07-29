package fr.catlean.monolithic.backend.domain.service.account;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.Team;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import fr.catlean.monolithic.backend.domain.port.in.TeamFacadeAdapter;
import fr.catlean.monolithic.backend.domain.port.out.AccountTeamStorage;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
public class TeamService implements TeamFacadeAdapter {

    private final AccountTeamStorage accountTeamStorage;

    @Override
    public List<Team> createTeamsForNameAndRepositoriesAndUser(final Map<String, List<String>> repositoryIdsMappedToTeamName,
                                                               User user) throws CatleanException {
        user.hasConfiguredTeam();
        final List<Team> teams = new ArrayList<>();
        repositoryIdsMappedToTeamName.forEach((teamName, repositoryIds) -> teams.add(
                Team.builder()
                        .name(teamName)
                        .repositories(repositoryIds.stream().map(id -> Repository.builder().id(id).build()).toList())
                        .organizationId(user.getOrganization().getId())
                        .build()
        ));
        return accountTeamStorage.createTeamsForUser(teams, user);
    }

    @Override
    public List<Team> getTeamsForOrganization(Organization organization) throws CatleanException {
        return accountTeamStorage.findByOrganization(organization);
    }

    @Override
    public void deleteForId(UUID teamId) throws CatleanException {
        accountTeamStorage.deleteById(teamId);
    }

    @Override
    public void update(Team team) throws CatleanException {
        accountTeamStorage.update(team);
    }
}
