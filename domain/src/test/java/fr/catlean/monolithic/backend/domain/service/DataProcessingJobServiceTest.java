package fr.catlean.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.job.Job;
import fr.catlean.monolithic.backend.domain.job.JobManager;
import fr.catlean.monolithic.backend.domain.job.runnable.CollectPullRequestsJobRunnable;
import fr.catlean.monolithic.backend.domain.job.runnable.CollectRepositoriesJobRunnable;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import fr.catlean.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import fr.catlean.monolithic.backend.domain.service.insights.PullRequestHistogramService;
import fr.catlean.monolithic.backend.domain.service.platform.vcs.RepositoryService;
import fr.catlean.monolithic.backend.domain.service.platform.vcs.VcsService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DataProcessingJobServiceTest {

    private final Faker faker = new Faker();
    private final Executor executor = new Executor() {
        @Override
        public void execute(Runnable command) {
            command.run();
        }
    };

    // TODO : add unit test raising CatleanException
    @Test
    void should_start_data_processing_job_given_an_organisation_name() throws CatleanException {
        // Given
        final JobManager jobManager = mock(JobManager.class);
        final VcsService vcsService = mock(VcsService.class);
        final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter =
                mock(AccountOrganizationStorageAdapter.class);
        final PullRequestHistogramService pullRequestHistogramService = mock(PullRequestHistogramService.class);
        final RepositoryService repositoryService = mock(RepositoryService.class);
        final DataProcessingJobService dataProcessingJobService =
                new DataProcessingJobService(vcsService,
                        accountOrganizationStorageAdapter, pullRequestHistogramService, repositoryService, jobManager);
        final String organisationName = faker.name().username();
        final Organization organisation = Organization.builder().id(UUID.randomUUID()).name(organisationName)
                .vcsOrganization(VcsOrganization.builder().build()).build();
        final List<PullRequest> pullRequests = List.of(PullRequest.builder().vcsId(faker.pokemon().name()).build(),
                PullRequest.builder().vcsId(faker.hacker().abbreviation()).build());
        final List<Repository> repositories = List.of(
                Repository.builder().name(faker.name().firstName()).vcsOrganizationName(organisationName).build(),
                Repository.builder().name(faker.name().firstName()).vcsOrganizationName(organisationName).build(),
                Repository.builder().name(faker.name().firstName()).vcsOrganizationName(organisationName).build()
        );

        // When
        final ArgumentCaptor<Job> jobArgumentCaptor = ArgumentCaptor.forClass(Job.class);
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
        verify(jobManager, times(1)).start(jobArgumentCaptor.capture());
        assertThat(jobArgumentCaptor.getAllValues()).hasSize(1);
        assertThat(jobArgumentCaptor.getAllValues().get(0).getCode()).isEqualTo(CollectRepositoriesJobRunnable.JOB_CODE);
        assertThat(jobArgumentCaptor.getAllValues().get(0).getOrganizationId()).isEqualTo(organisation.getId());
        assertThat(jobArgumentCaptor.getAllValues().get(0).getNextJob()).isNotNull();
        assertThat(jobArgumentCaptor.getAllValues().get(0).getNextJob().getCode()).isEqualTo(CollectPullRequestsJobRunnable.JOB_CODE);
        assertThat(jobArgumentCaptor.getAllValues().get(0).getNextJob().getOrganizationId()).isEqualTo(organisation.getId());

    }
}
