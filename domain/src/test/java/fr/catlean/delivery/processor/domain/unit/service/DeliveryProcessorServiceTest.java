package fr.catlean.delivery.processor.domain.unit.service;

import com.github.javafaker.Faker;
import fr.catlean.delivery.processor.domain.command.DeliveryCommand;
import fr.catlean.delivery.processor.domain.model.PullRequest;
import fr.catlean.delivery.processor.domain.model.Repository;
import fr.catlean.delivery.processor.domain.model.account.OrganisationAccount;
import fr.catlean.delivery.processor.domain.model.account.VcsConfiguration;
import fr.catlean.delivery.processor.domain.port.out.ExpositionStorage;
import fr.catlean.delivery.processor.domain.query.DeliveryQuery;
import fr.catlean.delivery.processor.domain.service.DeliveryProcessorService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DeliveryProcessorServiceTest {

    private final Faker faker = Faker.instance();

    @Test
    void should_return_empty_list_for_repository_without_pull_request() {
        // Given
        final DeliveryCommand deliveryCommand = mock(DeliveryCommand.class);
        final DeliveryQuery deliveryQuery = mock(DeliveryQuery.class);
        final ExpositionStorage expositionStorage = mock(ExpositionStorage.class);
        final DeliveryProcessorService deliveryProcessorService = new DeliveryProcessorService(deliveryCommand,
                deliveryQuery, expositionStorage);
        final String organisationName = faker.name().name();
        final OrganisationAccount organisationAccount = OrganisationAccount.builder()
                .vcsConfiguration(VcsConfiguration.builder().build()).name(organisationName).build();

        // When
        final Repository repo1 =
                Repository.builder().name(faker.pokemon().name() + "1").organisationName(organisationName).build();
        final Repository repo2 =
                Repository.builder().name(faker.pokemon().name() + "2").organisationName(organisationName).build();
        when(deliveryQuery.readRepositoriesForOrganisation(organisationAccount))
                .thenReturn(
                        List.of(
                                repo1,
                                repo2
                        )
                );
        final PullRequest pr11 = PullRequest.builder().id("github-11").build();
        final PullRequest pr12 = PullRequest.builder().id("github-12").build();
        when(deliveryQuery.readPullRequestsForRepository(repo1))
                .thenReturn(
                        List.of(
                                pr11,
                                pr12
                        )
                );
        when(deliveryQuery.readPullRequestsForRepository(repo2))
                .thenReturn(List.of());
        final List<PullRequest> pullRequestList =
                deliveryProcessorService.collectPullRequestsForOrganisation(organisationAccount);

        // Then
        assertThat(pullRequestList).containsAll(List.of(pr11, pr12));
    }

    @Test
    void should_compute_collect_all_pull_requests_details_for_a_given_organisation_account() {
        // Given
        final DeliveryCommand deliveryCommand = mock(DeliveryCommand.class);
        final DeliveryQuery deliveryQuery = mock(DeliveryQuery.class);
        final ExpositionStorage expositionStorage = mock(ExpositionStorage.class);
        final DeliveryProcessorService deliveryProcessorService = new DeliveryProcessorService(deliveryCommand,
                deliveryQuery, expositionStorage);
        final String vcsOrganisationName = faker.name().name();
        final String organisationName = faker.name().lastName();
        final String repo1Name = faker.pokemon().name() + "1";
        final String repo2Name = faker.pokemon().name() + "2";
        final String repo3Name = faker.pokemon().name() + "3";
        final String repo4Name = faker.pokemon().name() + "4";
        final OrganisationAccount organisationAccount = OrganisationAccount.builder().name(organisationName)
                .vcsConfiguration(
                        VcsConfiguration.builder()
                                .organisationName(vcsOrganisationName)
                                .build()
                )
                .build();
        final String team1Name = faker.pokemon().name() + "team1";
        final String team2Name = faker.pokemon().name() + "team2";
        organisationAccount.addTeam(team1Name, List.of(repo1Name, repo3Name), 1000, 5);
        organisationAccount.addTeam(team2Name, List.of(repo4Name), 500, 7);

        // When
        final Repository repo1 =
                Repository.builder().name(repo1Name).organisationName(vcsOrganisationName).build();
        final Repository repo2 =
                Repository.builder().name(repo2Name).organisationName(vcsOrganisationName).build();
        final Repository repo3 =
                Repository.builder().name(repo3Name).organisationName(vcsOrganisationName).build();
        final Repository repo4 =
                Repository.builder().name(repo4Name).organisationName(vcsOrganisationName).build();

        when(deliveryQuery.readRepositoriesForOrganisation(organisationAccount))
                .thenReturn(
                        List.of(
                                repo1,
                                repo2,
                                repo3,
                                repo4
                        )
                );
        final PullRequest pr11 = PullRequest.builder().id("github-11").build();
        final PullRequest pr12 = PullRequest.builder().id("github-12").build();
        when(deliveryQuery.readPullRequestsForRepository(repo1))
                .thenReturn(
                        List.of(
                                pr11,
                                pr12
                        )
                );
        final PullRequest pr21 = PullRequest.builder().id("github-21").build();
        final PullRequest pr22 = PullRequest.builder().id("github-22").build();
        when(deliveryQuery.readPullRequestsForRepository(repo2))
                .thenReturn(
                        List.of(
                                pr21,
                                pr22
                        )
                );
        final PullRequest pr31 = PullRequest.builder().id("github-31").build();
        final PullRequest pr32 = PullRequest.builder().id("github-32").build();
        when(deliveryQuery.readPullRequestsForRepository(repo3))
                .thenReturn(
                        List.of(
                                pr31,
                                pr32
                        )
                );
        final PullRequest pr41 = PullRequest.builder().id("github-41").build();
        final PullRequest pr42 = PullRequest.builder().id("github-42").build();
        when(deliveryQuery.readPullRequestsForRepository(repo4))
                .thenReturn(
                        List.of(
                                pr41,
                                pr42
                        )
                );
        List<PullRequest> pullRequestList =
                deliveryProcessorService.collectPullRequestsForOrganisation(organisationAccount);

        // Then
        assertThat(pullRequestList).containsAll(List.of(pr11, pr12, pr31, pr32, pr41, pr42));
        assertThat(pullRequestList).doesNotContain(pr21, pr22);
        verify(expositionStorage, times(1)).savePullRequestDetails(pullRequestList);
    }
}
