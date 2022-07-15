package fr.catlean.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.command.DeliveryCommand;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.PullRequest;
import fr.catlean.monolithic.backend.domain.model.Repository;
import fr.catlean.monolithic.backend.domain.model.account.OrganizationAccount;
import fr.catlean.monolithic.backend.domain.model.account.VcsConfiguration;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import fr.catlean.monolithic.backend.domain.query.DeliveryQuery;
import fr.catlean.monolithic.backend.domain.service.DeliveryProcessorService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DeliveryProcessorServiceTest {

    private final Faker faker = Faker.instance();

    @Test
    void should_return_empty_list_for_repository_without_pull_request() throws CatleanException {
        // Given
        final DeliveryCommand deliveryCommand = mock(DeliveryCommand.class);
        final DeliveryQuery deliveryQuery = mock(DeliveryQuery.class);
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final DeliveryProcessorService deliveryProcessorService = new DeliveryProcessorService(deliveryCommand,
                deliveryQuery, expositionStorageAdapter);
        final String organizationName = faker.name().name();
        final OrganizationAccount organizationAccount = OrganizationAccount.builder()
                .vcsConfiguration(VcsConfiguration.builder().build()).name(organizationName).build();

        // When
        final Repository repo1 =
                Repository.builder().name(faker.pokemon().name() + "1").organizationName(organizationName).build();
        final Repository repo2 =
                Repository.builder().name(faker.pokemon().name() + "2").organizationName(organizationName).build();
        when(deliveryQuery.readRepositoriesForOrganization(organizationAccount))
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
                deliveryProcessorService.collectPullRequestsForOrganization(organizationAccount);

        // Then
        assertThat(pullRequestList).containsAll(List.of(pr11, pr12));
    }

    @Test
    void should_compute_collect_all_pull_requests_details_for_a_given_organization_account() throws CatleanException {
        // Given
        final DeliveryCommand deliveryCommand = mock(DeliveryCommand.class);
        final DeliveryQuery deliveryQuery = mock(DeliveryQuery.class);
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final DeliveryProcessorService deliveryProcessorService = new DeliveryProcessorService(deliveryCommand,
                deliveryQuery, expositionStorageAdapter);
        final String vcsOrganizationName = faker.name().name();
        final String organizationName = faker.name().lastName();
        final String repo1Name = faker.pokemon().name() + "1";
        final String repo2Name = faker.pokemon().name() + "2";
        final String repo3Name = faker.pokemon().name() + "3";
        final String repo4Name = faker.pokemon().name() + "4";
        final OrganizationAccount organizationAccount = OrganizationAccount.builder().name(organizationName)
                .vcsConfiguration(
                        VcsConfiguration.builder()
                                .organizationName(vcsOrganizationName)
                                .build()
                )
                .build();
        final String team1Name = faker.pokemon().name() + "team1";
        final String team2Name = faker.pokemon().name() + "team2";
        organizationAccount.addTeam(team1Name, List.of(repo1Name, repo3Name), 1000, 5);
        organizationAccount.addTeam(team2Name, List.of(repo4Name), 500, 7);

        // When
        final Repository repo1 =
                Repository.builder().name(repo1Name).organizationName(vcsOrganizationName).build();
        final Repository repo2 =
                Repository.builder().name(repo2Name).organizationName(vcsOrganizationName).build();
        final Repository repo3 =
                Repository.builder().name(repo3Name).organizationName(vcsOrganizationName).build();
        final Repository repo4 =
                Repository.builder().name(repo4Name).organizationName(vcsOrganizationName).build();

        when(deliveryQuery.readRepositoriesForOrganization(organizationAccount))
                .thenReturn(
                        List.of(
                                repo1,
                                repo2,
                                repo3,
                                repo4
                        )
                );
        final PullRequest pr11 = PullRequest.builder().id("github-11").repository(repo1Name).build();
        final PullRequest pr12 = PullRequest.builder().id("github-12").repository(repo1Name).build();
        when(deliveryQuery.readPullRequestsForRepository(repo1))
                .thenReturn(
                        List.of(
                                pr11,
                                pr12
                        )
                );
        final PullRequest pr21 = PullRequest.builder().id("github-21").repository(repo2Name).build();
        final PullRequest pr22 = PullRequest.builder().id("github-22").repository(repo2Name).build();
        when(deliveryQuery.readPullRequestsForRepository(repo2))
                .thenReturn(
                        List.of(
                                pr21,
                                pr22
                        )
                );
        final PullRequest pr31 = PullRequest.builder().id("github-31").repository(repo3Name).build();
        final PullRequest pr32 = PullRequest.builder().id("github-32").repository(repo3Name).build();
        when(deliveryQuery.readPullRequestsForRepository(repo3))
                .thenReturn(
                        List.of(
                                pr31,
                                pr32
                        )
                );
        final PullRequest pr41 = PullRequest.builder().id("github-41").repository(repo4Name).build();
        final PullRequest pr42 = PullRequest.builder().id("github-42").repository(repo4Name).build();
        when(deliveryQuery.readPullRequestsForRepository(repo4))
                .thenReturn(
                        List.of(
                                pr41,
                                pr42
                        )
                );
        List<PullRequest> pullRequestList =
                deliveryProcessorService.collectPullRequestsForOrganization(organizationAccount);

        // Then
        assertThat(pullRequestList.stream().map(PullRequest::getId)).containsAll(
                Stream.of(pr11, pr12, pr31, pr32, pr41, pr42).map(PullRequest::getId).toList());
        assertThat(pullRequestList.stream().map(PullRequest::getId)).doesNotContain(pr21.getId(), pr22.getId());
        for (PullRequest pullRequest : pullRequestList) {
            assertThat(pullRequest.getTeam()).isNotNull();
            assertThat(pullRequest.getVcsOrganization()).isNotNull();
            assertThat(pullRequest.getOrganization()).isNotNull();
        }
        verify(expositionStorageAdapter, times(1)).savePullRequestDetails(pullRequestList);
    }
}
