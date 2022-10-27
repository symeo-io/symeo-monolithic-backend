package io.symeo.monolithic.backend.job.domain.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import io.symeo.monolithic.backend.job.domain.model.job.Job;
import io.symeo.monolithic.backend.job.domain.model.job.JobManager;
import io.symeo.monolithic.backend.job.domain.model.job.runnable.CollectRepositoriesJobRunnable;
import io.symeo.monolithic.backend.job.domain.model.job.runnable.CollectVcsDataForRepositoriesAndDatesJobRunnable;
import io.symeo.monolithic.backend.job.domain.model.vcs.Repository;
import io.symeo.monolithic.backend.job.domain.model.vcs.VcsOrganization;
import io.symeo.monolithic.backend.job.domain.port.out.JobExpositionStorageAdapter;
import io.symeo.monolithic.backend.job.domain.port.out.JobStorage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class JobServiceTest {

    private static final Faker faker = new Faker();

    @Test
    void should_start_to_collect_repositories_given_an_organization_id_and_a_vcs_organization_id() throws SymeoException {
        // Given
        final JobExpositionStorageAdapter jobExpositionStorageAdapter = mock(JobExpositionStorageAdapter.class);
        final VcsDataProcessingService vcsDataProcessingService = mock(VcsDataProcessingService.class);
        final JobManager jobManager = mock(JobManager.class);
        final JobStorage jobStorage = mock(JobStorage.class);
        final JobService jobService = new JobService(
                jobExpositionStorageAdapter,
                jobStorage,
                vcsDataProcessingService,
                jobManager
        );
        final UUID organizationId = UUID.randomUUID();
        final Long vcsOrganizationId = faker.number().randomNumber();
        final VcsOrganization vcsOrganization = VcsOrganization.builder()
                .externalId(faker.pokemon().name())
                .organizationId(UUID.randomUUID())
                .id(vcsOrganizationId)
                .vcsId(faker.howIMetYourMother().quote())
                .name(faker.robin().quote())
                .build();

        // When
        when(jobExpositionStorageAdapter.findVcsOrganizationByIdAndOrganizationId(vcsOrganizationId, organizationId))
                .thenReturn(
                        Optional.of(vcsOrganization)
                );
        jobService.startToCollectRepositoriesForOrganizationIdAndVcsOrganizationId(
                organizationId, vcsOrganizationId
        );

        // Then
        final ArgumentCaptor<Job> jobArgumentCaptor = ArgumentCaptor.forClass(Job.class);
        verify(jobManager, times(1))
                .start(jobArgumentCaptor.capture());
        final Job job = jobArgumentCaptor.getValue();
        assertThat(job.getCode()).isEqualTo(CollectRepositoriesJobRunnable.JOB_CODE);
        assertThat(job.getStatus()).isEqualTo(Job.CREATED);
        assertThat(job.getOrganizationId()).isEqualTo(organizationId);
        assertThat(job.getJobRunnable()).isInstanceOf(CollectRepositoriesJobRunnable.class);
    }

    @Test
    void should_validate_vcs_organization() throws SymeoException {
        // Given
        final JobExpositionStorageAdapter jobExpositionStorageAdapter = mock(JobExpositionStorageAdapter.class);
        final VcsDataProcessingService vcsDataProcessingService = mock(VcsDataProcessingService.class);
        final JobManager jobManager = mock(JobManager.class);
        final JobStorage jobStorage = mock(JobStorage.class);
        final JobService jobService = new JobService(
                jobExpositionStorageAdapter,
                jobStorage,
                vcsDataProcessingService,
                jobManager
        );
        final UUID organizationId = UUID.randomUUID();
        final Long vcsOrganizationId = faker.number().randomNumber();

        // When
        when(jobExpositionStorageAdapter.findVcsOrganizationByIdAndOrganizationId(vcsOrganizationId, organizationId))
                .thenReturn(Optional.empty());
        SymeoException symeoException = null;
        try {
            jobService.startToCollectRepositoriesForOrganizationIdAndVcsOrganizationId(organizationId,
                    vcsOrganizationId);
        } catch (SymeoException e) {
            symeoException = e;
        }

        // Then
        assertThat(symeoException).isNotNull();
        assertThat(symeoException.getCode()).isEqualTo(SymeoExceptionCode.VCS_ORGANIZATION_NOT_FOUND);
        assertThat(symeoException.getMessage())
                .isEqualTo(String.format("VcsOrganization not found for vcsOrganizationId %s and organizationId %s",
                        vcsOrganizationId, organizationId));
    }

    @Test
    void should_start_to_collect_vcs_data_given_an_organization_id_and_a_list_of_repositories_id() throws SymeoException {
        // Given
        final JobExpositionStorageAdapter jobExpositionStorageAdapter = mock(JobExpositionStorageAdapter.class);
        final VcsDataProcessingService vcsDataProcessingService = mock(VcsDataProcessingService.class);
        final JobManager jobManager = mock(JobManager.class);
        final JobStorage jobStorage = mock(JobStorage.class);
        final JobService jobService = new JobService(
                jobExpositionStorageAdapter,
                jobStorage,
                vcsDataProcessingService,
                jobManager
        );
        final UUID organizationId = UUID.randomUUID();
        final List<String> repositoryIds = List.of(faker.ancient().god(), faker.ancient().hero());
        final List<Repository> repositories = List.of(
                Repository.builder()
                        .name(faker.name().firstName() + "-1")
                        .vcsOrganizationName(faker.rickAndMorty().location() + "-1")
                        .organizationId(UUID.randomUUID())
                        .vcsOrganizationId(faker.rickAndMorty().character() + "-1")
                        .id(faker.idNumber().invalid() + "-1")
                        .build(),
                Repository.builder()
                        .name(faker.name().firstName() + "-2")
                        .vcsOrganizationName(faker.rickAndMorty().location() + "-2")
                        .organizationId(UUID.randomUUID())
                        .vcsOrganizationId(faker.rickAndMorty().character() + "-2")
                        .id(faker.idNumber().invalid() + "-2")
                        .build()
        );

        // When
        when(jobExpositionStorageAdapter.findAllRepositoriesByIds(repositoryIds))
                .thenReturn(repositories);
        jobService.startToCollectVcsDataForOrganizationIdAndRepositoryIds(organizationId, repositoryIds);

        // Then
        final ArgumentCaptor<Job> jobArgumentCaptor = ArgumentCaptor.forClass(Job.class);
        verify(jobManager, times(1))
                .start(jobArgumentCaptor.capture());
        final Job job = jobArgumentCaptor.getValue();
        assertThat(job.getCode()).isEqualTo(CollectVcsDataForRepositoriesAndDatesJobRunnable.JOB_CODE);
        assertThat(job.getStatus()).isEqualTo(Job.CREATED);
        assertThat(job.getOrganizationId()).isEqualTo(organizationId);
        assertThat(job.getJobRunnable()).isInstanceOf(CollectVcsDataForRepositoriesAndDatesJobRunnable.class);
    }

    @Test
    void should_validate_repositories() throws SymeoException {
        // Given
        final JobExpositionStorageAdapter jobExpositionStorageAdapter = mock(JobExpositionStorageAdapter.class);
        final VcsDataProcessingService vcsDataProcessingService = mock(VcsDataProcessingService.class);
        final JobManager jobManager = mock(JobManager.class);
        final JobStorage jobStorage = mock(JobStorage.class);
        final JobService jobService = new JobService(
                jobExpositionStorageAdapter,
                jobStorage,
                vcsDataProcessingService,
                jobManager
        );
        final UUID organizationId = UUID.randomUUID();
        final List<String> repositoryIds = List.of(faker.ancient().god(), faker.ancient().hero());

        // When
        when(jobExpositionStorageAdapter.findAllRepositoriesByIds(repositoryIds))
                .thenReturn(List.of(Repository.builder()
                        .id(repositoryIds.get(0))
                        .vcsOrganizationId(faker.rickAndMorty().character())
                        .organizationId(organizationId)
                        .name(faker.name().firstName())
                        .vcsOrganizationName(faker.rickAndMorty().location())
                        .build()));
        SymeoException symeoException = null;
        try {
            jobService.startToCollectVcsDataForOrganizationIdAndRepositoryIds(organizationId, repositoryIds);
        } catch (SymeoException e) {
            symeoException = e;
        }

        // Then
        assertThat(symeoException).isNotNull();
        assertThat(symeoException.getCode()).isEqualTo(SymeoExceptionCode.REPOSITORIES_NOT_FOUND);
        assertThat(symeoException.getMessage()).isEqualTo(String.format("Repositories %s not found for " +
                "organizationId %s", List.of(repositoryIds.get(1)), organizationId));
    }
}