package fr.catlean.delivery.processor.domain.unit.domain.account;

import com.github.javafaker.Faker;
import fr.catlean.delivery.processor.domain.model.account.OrganisationAccount;
import fr.catlean.delivery.processor.domain.model.account.VcsConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OrganisationAccountTest {

    private final Faker faker = new Faker();

    @Test
    void should_build_organisation_account_with_teams() {
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
        OrganisationAccount organisationAccount = OrganisationAccount.builder()
                .vcsConfiguration(VcsConfiguration.builder().organisationName(faker.animal().name()).build())
                .build();

        // When
        organisationAccount = organisationAccount.addTeam(team1Name, team1VcsRepositoryNames,
                team1PullRequestLineNumberLimit, team1PullRequestDayNumberLimit);
        organisationAccount = organisationAccount.addTeam(team2Name, team2VcsRepositoryNames,
                team2PullRequestLineNumberLimit, team2PullRequestDayNumberLimit);

        // Then
        assertThat(organisationAccount.getTeamAccounts()).hasSize(2);
        assertThat(organisationAccount.getVcsConfiguration().getVcsTeams()).hasSize(2);
        assertThat(organisationAccount.getVcsConfiguration().getVcsTeams().get(0).getVcsRepositoryNames()).containsAll(team1VcsRepositoryNames);
        assertThat(organisationAccount.getVcsConfiguration().getVcsTeams().get(0).getPullRequestDayNumberLimit()).isEqualTo(team1PullRequestDayNumberLimit);
        assertThat(organisationAccount.getVcsConfiguration().getVcsTeams().get(0).getPullRequestLineNumberLimit()).isEqualTo(team1PullRequestLineNumberLimit);
        assertThat(organisationAccount.getVcsConfiguration().getVcsTeams().get(1).getVcsRepositoryNames()).containsAll(team2VcsRepositoryNames);
        assertThat(organisationAccount.getVcsConfiguration().getVcsTeams().get(1).getPullRequestDayNumberLimit()).isEqualTo(team2PullRequestDayNumberLimit);
        assertThat(organisationAccount.getVcsConfiguration().getVcsTeams().get(1).getPullRequestLineNumberLimit()).isEqualTo(team2PullRequestLineNumberLimit);
        assertThat(organisationAccount.getVcsConfiguration().getAllTeamsRepositories()).hasSize(6);
        assertThat(organisationAccount.getVcsConfiguration().getAllTeamsRepositories()).containsAll(team1VcsRepositoryNames);
        assertThat(organisationAccount.getVcsConfiguration().getAllTeamsRepositories()).containsAll(team2VcsRepositoryNames);
    }
}
