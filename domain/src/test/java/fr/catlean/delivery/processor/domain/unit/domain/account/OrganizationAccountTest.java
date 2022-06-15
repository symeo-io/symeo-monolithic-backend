package fr.catlean.delivery.processor.domain.unit.domain.account;

import com.github.javafaker.Faker;
import fr.catlean.delivery.processor.domain.model.account.OrganizationAccount;
import fr.catlean.delivery.processor.domain.model.account.VcsConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OrganizationAccountTest {

    private final Faker faker = new Faker();

    @Test
    void should_build_organization_account_with_teams() {
        // Given
        final String team1Name = faker.name().firstName();
        final String team2Name = faker.name().lastName();
        final int team1PullRequestLineNumberLimit = 1000;
        final int team2PullRequestLineNumberLimit = 500;
        final int team1PullRequestDayNumberLimit = 5;
        final int team2PullRequestDayNumberLimit = 7;
        final List<String> team1VcsRepositoryNames = List.of(faker.pokemon().name(), faker.harryPotter().house(),
                faker.shakespeare().romeoAndJulietQuote());
        final List<String> team2VcsRepositoryNames = List.of(faker.pokemon().name(), faker.harryPotter().house(),
                faker.shakespeare().romeoAndJulietQuote());
        OrganizationAccount organizationAccount = OrganizationAccount.builder()
                .vcsConfiguration(VcsConfiguration.builder().organizationName(faker.animal().name()).build())
                .build();

        // When
        organizationAccount = organizationAccount.addTeam(team1Name, team1VcsRepositoryNames,
                team1PullRequestLineNumberLimit, team1PullRequestDayNumberLimit);
        organizationAccount = organizationAccount.addTeam(team2Name, team2VcsRepositoryNames,
                team2PullRequestLineNumberLimit, team2PullRequestDayNumberLimit);

        // Then
        assertThat(organizationAccount.getTeamAccounts()).hasSize(2);
        assertThat(organizationAccount.getVcsConfiguration().getVcsTeams()).hasSize(2);
        assertThat(organizationAccount.getVcsConfiguration().getVcsTeams().get(0).getVcsRepositoryNames()).containsAll(team1VcsRepositoryNames);
        assertThat(organizationAccount.getVcsConfiguration().getVcsTeams().get(0).getPullRequestDayNumberLimit()).isEqualTo(team1PullRequestDayNumberLimit);
        assertThat(organizationAccount.getVcsConfiguration().getVcsTeams().get(0).getPullRequestLineNumberLimit()).isEqualTo(team1PullRequestLineNumberLimit);
        assertThat(organizationAccount.getVcsConfiguration().getVcsTeams().get(1).getVcsRepositoryNames()).containsAll(team2VcsRepositoryNames);
        assertThat(organizationAccount.getVcsConfiguration().getVcsTeams().get(1).getPullRequestDayNumberLimit()).isEqualTo(team2PullRequestDayNumberLimit);
        assertThat(organizationAccount.getVcsConfiguration().getVcsTeams().get(1).getPullRequestLineNumberLimit()).isEqualTo(team2PullRequestLineNumberLimit);
        assertThat(organizationAccount.getVcsConfiguration().getAllTeamsRepositories()).hasSize(6);
        assertThat(organizationAccount.getVcsConfiguration().getAllTeamsRepositories()).containsAll(team1VcsRepositoryNames);
        assertThat(organizationAccount.getVcsConfiguration().getAllTeamsRepositories()).containsAll(team2VcsRepositoryNames);
    }
}
