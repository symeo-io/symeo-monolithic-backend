package fr.catlean.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.job.DataProcessingJobService;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import fr.catlean.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import fr.catlean.monolithic.backend.domain.service.insights.PullRequesHistogramtService;
import fr.catlean.monolithic.backend.domain.service.platform.vcs.RepositoryService;
import fr.catlean.monolithic.backend.domain.service.platform.vcs.VcsService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

public class DataProcessingJobServiceTest {

    private final Faker faker = new Faker();

    // TODO : add unit test raising CatleanException
    @Test
    void should_start_data_processing_job_given_an_organisation_name() throws CatleanException {
        // Given
        final VcsService vcsService = mock(VcsService.class);
        final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter = mock(AccountOrganizationStorageAdapter.class);
        final PullRequesHistogramtService pullRequesHistogramtService = mock(PullRequesHistogramtService.class);
        final RepositoryService repositoryService = mock(RepositoryService.class);
        final DataProcessingJobService dataProcessingJobService =
                new DataProcessingJobService(vcsService,
                        accountOrganizationStorageAdapter, pullRequesHistogramtService, repositoryService);
        final String organisationName = faker.name().username();
        final Organization organisation = Organization.builder().name(organisationName)
                .vcsOrganization(VcsOrganization.builder().build()).build();
        final List<PullRequest> pullRequests = List.of(PullRequest.builder().vcsId(faker.pokemon().name()).build(),
                PullRequest.builder().vcsId(faker.hacker().abbreviation()).build());
        final List<Repository> repositories = List.of(
                Repository.builder().name(faker.name().firstName()).vcsOrganizationName(organisationName).build(),
                Repository.builder().name(faker.name().firstName()).vcsOrganizationName(organisationName).build(),
                Repository.builder().name(faker.name().firstName()).vcsOrganizationName(organisationName).build()
        );

        // When
        when(accountOrganizationStorageAdapter.findVcsOrganizationForName(organisationName)).thenReturn(
                organisation
        );
        when(vcsService.collectPullRequestsForOrganization(organisation)).thenReturn(
                pullRequests
        );
        when(vcsService.collectRepositoriesForOrganization(organisation)).thenReturn(
                repositories
        );
        dataProcessingJobService.start(organisationName);

        // Then
        verify(repositoryService, times(1)).saveRepositories(
                repositories.stream().map(repository -> repository.toBuilder().organization(organisation).build()).toList()
        );
        verify(pullRequesHistogramtService, times(1)).computeAndSavePullRequestSizeHistogram(pullRequests, organisation);
        verify(pullRequesHistogramtService, times(1)).computeAndSavePullRequestTimeHistogram(pullRequests, organisation);
        verify(pullRequesHistogramtService, times(1)).savePullRequests(pullRequests);

    }
}
