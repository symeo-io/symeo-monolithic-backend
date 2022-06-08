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
        final List<String> team1VcsRepositoryNames = List.of(faker.pokemon().name(), faker.harryPotter().house(),
                faker.shakespeare().romeoAndJulietQuote());
        final List<String> team2VcsRepositoryNames = List.of(faker.pokemon().name(), faker.harryPotter().house(),
                faker.shakespeare().romeoAndJulietQuote());
        OrganisationAccount organisationAccount = OrganisationAccount.builder()
                .vcsConfiguration(VcsConfiguration.builder().organisationName(faker.animal().name()).build())
                .build();

        // When
        organisationAccount = organisationAccount.addTeam(team1Name, team1VcsRepositoryNames);
        organisationAccount = organisationAccount.addTeam(team2Name, team2VcsRepositoryNames);

        // Then
        assertThat(organisationAccount.getTeamAccounts()).hasSize(2);
        assertThat(organisationAccount.getVcsConfiguration().getVcsTeams()).hasSize(2);
        assertThat(organisationAccount.getVcsConfiguration().getVcsTeams().get(0).getVcsRepositoryNames()).containsAll(team1VcsRepositoryNames);
        assertThat(organisationAccount.getVcsConfiguration().getVcsTeams().get(1).getVcsRepositoryNames()).containsAll(team2VcsRepositoryNames);
        assertThat(organisationAccount.getVcsConfiguration().getAllTeamsRepositories()).hasSize(6);
        assertThat(organisationAccount.getVcsConfiguration().getAllTeamsRepositories()).containsAll(team1VcsRepositoryNames);
        assertThat(organisationAccount.getVcsConfiguration().getAllTeamsRepositories()).containsAll(team2VcsRepositoryNames);
    }
}
