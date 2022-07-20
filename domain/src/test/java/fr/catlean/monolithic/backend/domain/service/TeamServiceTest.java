package fr.catlean.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.Team;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.model.account.VcsConfiguration;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import fr.catlean.monolithic.backend.domain.port.out.AccountTeamStorage;
import fr.catlean.monolithic.backend.domain.service.account.TeamService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TeamServiceTest {

    private final Faker faker = new Faker();

    @Test
    void should_create_and_return_teams() throws CatleanException {
        // Given
        final AccountTeamStorage accountTeamStorage = mock(AccountTeamStorage.class);
        final TeamService teamService = new TeamService(accountTeamStorage);
        final Organization organization =
                Organization.builder().id(UUID.randomUUID()).vcsConfiguration(VcsConfiguration.builder().build()).build();
        final String teamName1 = faker.name().firstName();
        final String teamName2 = faker.name().lastName();
        final List<Integer> repositoryIds1 = List.of(1, 2, 3);
        final List<Integer> repositoryIds2 = List.of(4, 5, 6);
        final Team team1 = Team.builder().name(teamName1).build();
        final Team team2 = Team.builder().name(teamName2).build();
        final Map<String, List<Integer>> repositoryIdsMappedToTeamName = Map.of(
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
                .repositories(repositoryIds1.stream().map(id -> Repository.builder().id(id).build()).toList())
                .build();
        final Team expectedTeam2 = team2.toBuilder()
                .id(UUID.randomUUID())
                .name(teamName2)
                .organizationId(organization.getId())
                .repositories(repositoryIds2.stream().map(id -> Repository.builder().id(id).build()).toList())
                .build();

        when(accountTeamStorage.createTeamsForUser(teamsArgumentCaptor.capture(), userArgumentCaptor.capture()))
                .thenReturn(List.of(expectedTeam1, expectedTeam2));
        teamService.createTeamsForNameAndRepositoriesAndUser(repositoryIdsMappedToTeamName,
                User.builder()
                        .organization(
                                Organization.builder().id(UUID.randomUUID())
                                        .vcsConfiguration(VcsConfiguration.builder().build())
                                        .build())
                        .build());

        // Then
        assertThat(userArgumentCaptor.getValue().getOnboarding().getHasConfiguredTeam()).isTrue();
        assertThat(teamsArgumentCaptor.getValue()).hasSize(2);
    }
}
