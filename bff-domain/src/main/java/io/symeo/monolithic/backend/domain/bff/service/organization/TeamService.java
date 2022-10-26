package io.symeo.monolithic.backend.domain.bff.service.organization;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.Team;
import io.symeo.monolithic.backend.domain.bff.model.account.User;
import io.symeo.monolithic.backend.domain.bff.model.vcs.RepositoryView;
import io.symeo.monolithic.backend.domain.bff.port.in.TeamFacadeAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.SymeoJobApiAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.TeamStorage;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
public class TeamService implements TeamFacadeAdapter {

    private final TeamStorage teamStorage;
    private final SymeoJobApiAdapter symeoJobApiAdapter;

    @Override
    public List<Team> createTeamsForNameAndRepositoriesAndUser(final Map<String, List<String>> repositoryIdsMappedToTeamName,
                                                               User user) throws SymeoException {
        user.hasConfiguredTeam();
        final List<Team> teams = new ArrayList<>();
        repositoryIdsMappedToTeamName.forEach((teamName, repositoryIds) -> teams.add(
                Team.builder()
                        .name(teamName)
                        .repositories(repositoryIds.stream().map(id -> RepositoryView.builder().id(id).build()).toList())
                        .organizationId(user.getOrganization().getId())
                        .build()
        ));
        final List<Team> createdTeams = teamStorage.createTeamsForUser(teams, user);
        for (Team createdTeam : createdTeams) {
            symeoJobApiAdapter.startJobForOrganizationIdAndTeamId(user.getOrganization().getId(),
                    createdTeam.getId());
        }
        return createdTeams;
    }

    @Override
    public List<Team> getTeamsForOrganization(Organization organization) throws SymeoException {
        return teamStorage.findByOrganization(organization);
    }

    @Override
    public void deleteForId(UUID teamId) throws SymeoException {
        teamStorage.deleteById(teamId);
    }

    @Override
    public void update(Team team) throws SymeoException {
        teamStorage.update(team);
        symeoJobApiAdapter.startJobForOrganizationIdAndTeamId(team.getOrganizationId(), team.getId());
    }
}
