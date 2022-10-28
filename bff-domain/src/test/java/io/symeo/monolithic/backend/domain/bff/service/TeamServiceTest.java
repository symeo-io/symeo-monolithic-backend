package io.symeo.monolithic.backend.domain.bff.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.Team;
import io.symeo.monolithic.backend.domain.bff.model.account.User;
import io.symeo.monolithic.backend.domain.bff.model.vcs.RepositoryView;
import io.symeo.monolithic.backend.domain.bff.port.out.SymeoDataProcessingJobApiAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.TeamStorage;
import io.symeo.monolithic.backend.domain.bff.service.organization.TeamService;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TeamServiceTest {

    private final Faker faker = new Faker();

    @Test
    void should_create_and_start_vcs_data_collection_then_return_teams() throws SymeoException {
        // Given
        final TeamStorage teamStorage = mock(TeamStorage.class);
        final SymeoDataProcessingJobApiAdapter symeoDataProcessingJobApiAdapter =
                mock(SymeoDataProcessingJobApiAdapter.class);
        final TeamService teamService = new TeamService(teamStorage, symeoDataProcessingJobApiAdapter);
        final Organization organization =
                Organization.builder().id(UUID.randomUUID()).vcsOrganization(Organization.VcsOrganization.builder().build()).build();
        final String teamName1 = faker.name().firstName();
        final String teamName2 = faker.name().lastName();
        final List<String> repositoryIds1 = List.of("1L", "2L", "3L");
        final List<String> repositoryIds2 = List.of("4L", "5L", "6L");
        final Team team1 = Team.builder().name(teamName1).build();
        final Team team2 = Team.builder().name(teamName2).build();
        final Map<String, List<String>> repositoryIdsMappedToTeamName = Map.of(
                teamName1, repositoryIds1,
                teamName2, repositoryIds2
        );

        // When
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<List<Team>> teamsArgumentCaptor = ArgumentCaptor.forClass(List.class);
        final Team expectedTeam1 = team1.toBuilder()
                .id(UUID.randomUUID())
                .name(teamName1)
                .organizationId(organization.getId())
                .repositories(repositoryIds1.stream().map(id -> RepositoryView.builder().id(id).build()).toList())
                .build();
        final Team expectedTeam2 = team2.toBuilder()
                .id(UUID.randomUUID())
                .name(teamName2)
                .organizationId(organization.getId())
                .repositories(repositoryIds2.stream().map(id -> RepositoryView.builder().id(id).build()).toList())
                .build();

        when(teamStorage.createTeamsForUser(teamsArgumentCaptor.capture(), userArgumentCaptor.capture()))
                .thenReturn(List.of(expectedTeam1, expectedTeam2));
        teamService.createTeamsForNameAndRepositoriesAndUser(repositoryIdsMappedToTeamName,
                User.builder()
                        .organizations(
                                List.of(
                                        Organization.builder().id(organization.getId())
                                                .vcsOrganization(Organization.VcsOrganization.builder().build())
                                                .build()))
                        .build());

        // Then
        assertThat(userArgumentCaptor.getValue().getOnboarding().getHasConfiguredTeam()).isTrue();
        assertThat(teamsArgumentCaptor.getValue()).hasSize(2);
        verify(symeoDataProcessingJobApiAdapter, times(2)).startDataProcessingJobForOrganizationIdAndTeamIdAndRepositoryIds(any(), any(), any());
        verify(symeoDataProcessingJobApiAdapter, times(1)).startDataProcessingJobForOrganizationIdAndTeamIdAndRepositoryIds(organization.getId(),
                expectedTeam1.getId(), expectedTeam1.getRepositories().stream().map(RepositoryView::getId).toList());
        verify(symeoDataProcessingJobApiAdapter, times(1)).startDataProcessingJobForOrganizationIdAndTeamIdAndRepositoryIds(organization.getId(),
                expectedTeam2.getId(), expectedTeam2.getRepositories().stream().map(RepositoryView::getId).toList());
    }

    @Test
    void should_return_teams_for_organization() throws SymeoException {
        // Given
        final TeamStorage teamStorage = mock(TeamStorage.class);
        final TeamService teamService = new TeamService(teamStorage, mock(SymeoDataProcessingJobApiAdapter.class));
        final Organization organization =
                Organization.builder().id(UUID.randomUUID()).vcsOrganization(Organization.VcsOrganization.builder().build()).build();

        // When
        final List<Team> teamList = List.of(Team.builder().build(), Team.builder().build());
        when(teamStorage.findByOrganization(organization)).thenReturn(teamList);
        final List<Team> teams = teamService.getTeamsForOrganization(organization);

        // Then
        assertThat(teams).hasSize(teamList.size());
    }

    @Test
    void should_delete_team_given_a_team_id() throws SymeoException {
        // Given
        final TeamStorage teamStorage = mock(TeamStorage.class);
        final TeamService teamService = new TeamService(teamStorage, mock(SymeoDataProcessingJobApiAdapter.class));
        final UUID teamId = UUID.randomUUID();

        // When
        teamService.deleteForId(teamId);

        // Then
        verify(teamStorage, times(1)).deleteById(teamId);
    }

    @Test
    void should_update_team_given_a_team() throws SymeoException {
        // Given
        final TeamStorage teamStorage = mock(TeamStorage.class);
        final TeamService teamService = new TeamService(teamStorage, mock(SymeoDataProcessingJobApiAdapter.class));
        final Team team = Team.builder().id(UUID.randomUUID()).repositories(List.of()).build();

        // When
        teamService.update(team);

        // Then
        verify(teamStorage, times(1)).update(team);
    }

    @Test
    void should_update_team_and_start_vcs_data_collection_for_a_team_given_a_team() throws SymeoException {
        // Given
        final TeamStorage teamStorage = mock(TeamStorage.class);
        final SymeoDataProcessingJobApiAdapter symeoDataProcessingJobApiAdapter =
                mock(SymeoDataProcessingJobApiAdapter.class);
        final TeamService teamService = new TeamService(teamStorage, symeoDataProcessingJobApiAdapter);
        final Team team = Team.builder()
                .id(UUID.randomUUID())
                .organizationId(UUID.randomUUID())
                .repositories(List.of(RepositoryView.builder().id(faker.name().firstName()).build()))
                .build();

        // When
        teamService.update(team);

        // Then
        verify(teamStorage, times(1)).update(team);
        verify(symeoDataProcessingJobApiAdapter, times(1)).startDataProcessingJobForOrganizationIdAndTeamIdAndRepositoryIds(
                team.getOrganizationId(), team.getId(),
                team.getRepositories().stream().map(RepositoryView::getId).toList());
    }
}
