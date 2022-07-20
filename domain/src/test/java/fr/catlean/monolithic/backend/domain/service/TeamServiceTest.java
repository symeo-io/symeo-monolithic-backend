package fr.catlean.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.Team;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.model.account.VcsConfiguration;
import fr.catlean.monolithic.backend.domain.port.out.AccountTeamStorage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TeamServiceTest {

    private final Faker faker = new Faker();

    @Test
    void should_create_and_return_team() throws CatleanException {
        // Given
        final AccountTeamStorage accountTeamStorage = mock(AccountTeamStorage.class);
        final TeamService teamService = new TeamService(accountTeamStorage);
        final Organization organization =
                Organization.builder().id(UUID.randomUUID()).vcsConfiguration(VcsConfiguration.builder().build()).build();
        final String teamName = faker.name().firstName();
        final List<Integer> repositoryIds = List.of(1, 2, 3);
        final Team team = Team.builder().name(teamName).build();

        // When
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        final Team expectedTeam = team.toBuilder()
                .id(UUID.randomUUID())
                .name(teamName)
                .organizationId(organization.getId())
                .repositories(repositoryIds.stream().map(id -> Repository.builder().id(id).build()).toList())
                .build();
        when(accountTeamStorage.createTeamForUser(any(), userArgumentCaptor.capture()))
                .thenReturn(expectedTeam);
        final Team result = teamService.createTeamForNameAndRepositoriesAndUser(teamName, repositoryIds,
                User.builder()
                        .organization(
                                Organization.builder().id(UUID.randomUUID())
                                        .vcsConfiguration(VcsConfiguration.builder().build())
                                        .build())
                        .build());

        // Then
        assertThat(result.getName()).isEqualTo(expectedTeam.getName());
        assertThat(result.getOrganizationId()).isEqualTo(expectedTeam.getOrganizationId());
        assertThat(result.getRepositories()).isEqualTo(expectedTeam.getRepositories());
        assertThat(userArgumentCaptor.getValue().getOnboarding().getHasConfiguredTeam()).isTrue();
    }
}
