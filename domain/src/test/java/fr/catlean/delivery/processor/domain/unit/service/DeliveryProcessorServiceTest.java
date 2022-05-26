package fr.catlean.delivery.processor.domain.unit.service;

import com.github.javafaker.Faker;
import fr.catlean.delivery.processor.domain.command.DeliveryCommand;
import fr.catlean.delivery.processor.domain.model.PullRequest;
import fr.catlean.delivery.processor.domain.model.Repository;
import fr.catlean.delivery.processor.domain.port.out.ExpositionStorage;
import fr.catlean.delivery.processor.domain.query.DeliveryQuery;
import fr.catlean.delivery.processor.domain.service.DeliveryProcessorService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        final String organisation = faker.name().name();

        // When
        final Repository repo1 =
                Repository.builder().name(faker.pokemon().name() + "1").organisationName(organisation).build();
        final Repository repo2 =
                Repository.builder().name(faker.pokemon().name() + "2").organisationName(organisation).build();
        when(deliveryQuery.readRepositoriesForOrganisation(organisation))
                .thenReturn(
                        List.of(
                                repo1,
                                repo2
                        )
                );
        final PullRequest pr11 = PullRequest.builder().id(11).build();
        final PullRequest pr12 = PullRequest.builder().id(12).build();
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
                deliveryProcessorService.collectPullRequestsForOrganisation(organisation);

        // Then
        Assertions.assertThat(pullRequestList).containsAll(List.of(pr11, pr12));
    }

    @Test
    void should_compute_collect_all_pull_requests_details_for_a_given_organisation() {
        // Given
        final DeliveryCommand deliveryCommand = mock(DeliveryCommand.class);
        final DeliveryQuery deliveryQuery = mock(DeliveryQuery.class);
        final ExpositionStorage expositionStorage = mock(ExpositionStorage.class);
        final DeliveryProcessorService deliveryProcessorService = new DeliveryProcessorService(deliveryCommand,
                deliveryQuery, expositionStorage);
        final String organisation = faker.name().name();

        // When
        final Repository repo1 =
                Repository.builder().name(faker.pokemon().name() + "1").organisationName(organisation).build();
        final Repository repo2 =
                Repository.builder().name(faker.pokemon().name() + "2").organisationName(organisation).build();
        final Repository repo3 =
                Repository.builder().name(faker.pokemon().name() + "3").organisationName(organisation).build();

        when(deliveryQuery.readRepositoriesForOrganisation(organisation))
                .thenReturn(
                        List.of(
                                repo1,
                                repo2,
                                repo3
                        )
                );
        final PullRequest pr11 = PullRequest.builder().id(11).build();
        final PullRequest pr12 = PullRequest.builder().id(12).build();
        when(deliveryQuery.readPullRequestsForRepository(repo1))
                .thenReturn(
                        List.of(
                                pr11,
                                pr12
                        )
                );
        final PullRequest pr21 = PullRequest.builder().id(21).build();
        final PullRequest pr22 = PullRequest.builder().id(22).build();
        when(deliveryQuery.readPullRequestsForRepository(repo2))
                .thenReturn(
                        List.of(
                                pr21,
                                pr22
                        )
                );
        final PullRequest pr31 = PullRequest.builder().id(31).build();
        final PullRequest pr32 = PullRequest.builder().id(32).build();
        when(deliveryQuery.readPullRequestsForRepository(repo3))
                .thenReturn(
                        List.of(
                                pr31,
                                pr32
                        )
                );
        List<PullRequest> pullRequestList = deliveryProcessorService.collectPullRequestsForOrganisation(organisation);

        // Then
        Assertions.assertThat(pullRequestList).containsAll(List.of(pr11, pr12, pr21, pr22, pr31, pr32));
        verify(expositionStorage, times(1)).savePullRequestDetails(pullRequestList);
    }
}
