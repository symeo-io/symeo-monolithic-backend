package fr.catlean.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.PullRequest;
import fr.catlean.monolithic.backend.domain.model.Repository;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.VcsConfiguration;
import fr.catlean.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
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
        final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter = mock(AccountOrganizationStorageAdapter.class);
        final PullRequestService pullRequestService = mock(PullRequestService.class);
        final RepositoryService repositoryService = mock(RepositoryService.class);
        final DataProcessingJobService dataProcessingJobService =
                new DataProcessingJobService(deliveryProcessorService,
                        accountOrganizationStorageAdapter, pullRequestService, repositoryService);
        final String organisationName = faker.name().username();
        final Organization organisation = Organization.builder().name(organisationName)
                .vcsConfiguration(VcsConfiguration.builder().build()).build();
        final List<PullRequest> pullRequests = List.of(PullRequest.builder().id(faker.pokemon().name()).build(),
                PullRequest.builder().id(faker.hacker().abbreviation()).build());
        final List<Repository> repositories = List.of(
                Repository.builder().name(faker.name().firstName()).vcsOrganizationName(organisationName).build(),
                Repository.builder().name(faker.name().firstName()).vcsOrganizationName(organisationName).build(),
                Repository.builder().name(faker.name().firstName()).vcsOrganizationName(organisationName).build()
        );

        // When
        when(accountOrganizationStorageAdapter.findOrganizationForName(organisationName)).thenReturn(
                organisation
        );
        when(deliveryProcessorService.collectPullRequestsForOrganization(organisation)).thenReturn(
                pullRequests
        );
        when(deliveryProcessorService.collectRepositoriesForOrganization(organisation)).thenReturn(
                repositories
        );
        dataProcessingJobService.start(organisationName);

        // Then
        verify(repositoryService, times(1)).saveRepositories(
                repositories.stream().map(repository -> repository.toBuilder().organization(organisation).build()).toList()
        );
        verify(pullRequestService, times(1)).computeAndSavePullRequestSizeHistogram(pullRequests, organisation);
        verify(pullRequestService, times(1)).computeAndSavePullRequestTimeHistogram(pullRequests, organisation);
        verify(pullRequestService, times(1)).savePullRequests(pullRequests);

    }
}
