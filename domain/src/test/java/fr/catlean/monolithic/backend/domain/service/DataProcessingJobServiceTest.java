package fr.catlean.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.PullRequest;
import fr.catlean.monolithic.backend.domain.model.Repository;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.VcsConfiguration;
import fr.catlean.monolithic.backend.domain.port.out.OrganizationStorageAdapter;
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
        final OrganizationStorageAdapter organizationStorageAdapter = mock(OrganizationStorageAdapter.class);
        final PullRequestService pullRequestService = mock(PullRequestService.class);
        final RepositoryService repositoryService = mock(RepositoryService.class);
        final DataProcessingJobService dataProcessingJobService =
                new DataProcessingJobService(deliveryProcessorService,
                        organizationStorageAdapter, pullRequestService, repositoryService);
        final String organisationName = faker.name().username();
        final Organization organisationAccount = Organization.builder().name(organisationName)
                .vcsConfiguration(VcsConfiguration.builder().build()).build();
        final List<PullRequest> pullRequests = List.of(PullRequest.builder().id(faker.pokemon().name()).build(),
                PullRequest.builder().id(faker.hacker().abbreviation()).build());
        final List<Repository> repositories = List.of(
                Repository.builder().name(faker.name().firstName()).vcsOrganizationName(organisationName).build(),
                Repository.builder().name(faker.name().firstName()).vcsOrganizationName(organisationName).build(),
                Repository.builder().name(faker.name().firstName()).vcsOrganizationName(organisationName).build()
        );

        // When
        when(organizationStorageAdapter.findOrganizationForName(organisationName)).thenReturn(
                organisationAccount
        );
        when(deliveryProcessorService.collectPullRequestsForOrganization(organisationAccount)).thenReturn(
                pullRequests
        );
        when(deliveryProcessorService.collectRepositoriesForOrganization(organisationAccount)).thenReturn(
                repositories
        );
        dataProcessingJobService.start(organisationName);

        // Then
        verify(repositoryService, times(1)).saveRepositories(repositories);
        verify(pullRequestService, times(1)).computeAndSavePullRequestSizeHistogram(pullRequests, organisationAccount);
        verify(pullRequestService, times(1)).computeAndSavePullRequestTimeHistogram(pullRequests, organisationAccount);
        verify(pullRequestService,times(1)).savePullRequests(pullRequests);

    }
}
