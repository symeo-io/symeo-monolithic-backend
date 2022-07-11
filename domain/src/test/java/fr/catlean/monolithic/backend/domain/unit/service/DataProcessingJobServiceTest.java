package fr.catlean.monolithic.backend.domain.unit.service;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.PullRequest;
import fr.catlean.monolithic.backend.domain.model.account.OrganizationAccount;
import fr.catlean.monolithic.backend.domain.model.account.VcsConfiguration;
import fr.catlean.monolithic.backend.domain.port.out.OrganizationAccountAdapter;
import fr.catlean.monolithic.backend.domain.service.DataProcessingJobService;
import fr.catlean.monolithic.backend.domain.service.DeliveryProcessorService;
import fr.catlean.monolithic.backend.domain.service.PullRequestService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

public class DataProcessingJobServiceTest {

    private final Faker faker = new Faker();

    // TODO : add unit test raising CatleanException
    @Test
    void should_start_data_processing_job_given_an_organisation_name() throws CatleanException {
        // Given
        final DeliveryProcessorService deliveryProcessorService = mock(DeliveryProcessorService.class);
        final OrganizationAccountAdapter organizationAccountAdapter = mock(OrganizationAccountAdapter.class);
        final PullRequestService pullRequestService = mock(PullRequestService.class);
        final DataProcessingJobService dataProcessingJobService =
                new DataProcessingJobService(deliveryProcessorService,
                        organizationAccountAdapter, pullRequestService);
        final String organisationName = faker.name().username();
        final OrganizationAccount organisationAccount = OrganizationAccount.builder().name(organisationName)
                .vcsConfiguration(VcsConfiguration.builder().build()).build();
        final List<PullRequest> pullRequests = List.of(PullRequest.builder().id(faker.pokemon().name()).build(),
                PullRequest.builder().id(faker.hacker().abbreviation()).build());

        // When
        when(organizationAccountAdapter.findOrganizationForName(organisationName)).thenReturn(
                organisationAccount
        );
        when(deliveryProcessorService.collectPullRequestsForOrganization(organisationAccount)).thenReturn(
                pullRequests
        );
        dataProcessingJobService.start(organisationName);

        // Then
        verify(pullRequestService, times(1)).computeAndSavePullRequestSizeHistogram(pullRequests, organisationAccount);
        verify(pullRequestService, times(1)).computeAndSavePullRequestTimeHistogram(pullRequests, organisationAccount);

    }
}
