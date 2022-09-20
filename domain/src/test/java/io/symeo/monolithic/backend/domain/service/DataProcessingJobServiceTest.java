package io.symeo.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.job.Job;
import io.symeo.monolithic.backend.domain.job.JobManager;
import io.symeo.monolithic.backend.domain.job.runnable.CollectRepositoriesJobRunnable;
import io.symeo.monolithic.backend.domain.job.runnable.CollectVcsDataForOrganizationJobRunnable;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import io.symeo.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.port.out.JobStorage;
import io.symeo.monolithic.backend.domain.port.out.SymeoJobApiAdapter;
import io.symeo.monolithic.backend.domain.service.platform.vcs.RepositoryService;
import io.symeo.monolithic.backend.domain.service.platform.vcs.VcsService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DataProcessingJobServiceTest {

    private final Faker faker = new Faker();

    // TODO : add unit test raising SymeoException
    @Test
    void should_collect_repositories_job_given_an_organization_id() throws SymeoException {
        // Given
        final JobManager jobManager = mock(JobManager.class);
        final VcsService vcsService = mock(VcsService.class);
        final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter =
                mock(AccountOrganizationStorageAdapter.class);
        final RepositoryService repositoryService = mock(RepositoryService.class);
        final SymeoJobApiAdapter symeoJobApiAdapter = mock(SymeoJobApiAdapter.class);
        final OrganizationSettingsService organizationSettingsService = mock(OrganizationSettingsService.class);
        final JobStorage jobStorage = mock(JobStorage.class);
        final DataProcessingJobService dataProcessingJobService =
                new DataProcessingJobService(vcsService,
                        accountOrganizationStorageAdapter, repositoryService, jobManager, symeoJobApiAdapter,
                        organizationSettingsService, mock(ExpositionStorageAdapter.class), jobStorage);
        final String organisationName = faker.name().username();
        final String vcsOrganizationId = faker.rickAndMorty().character();
        final UUID organizationId = UUID.randomUUID();
        final Organization organisation = Organization.builder().id(organizationId).name(organisationName)
                .vcsOrganization(VcsOrganization.builder().build()).build();
        final List<Repository> repositories = List.of(
                Repository.builder().name(faker.name().firstName()).vcsOrganizationId(vcsOrganizationId).build(),
                Repository.builder().name(faker.name().firstName()).vcsOrganizationId(vcsOrganizationId).build(),
                Repository.builder().name(faker.name().firstName()).vcsOrganizationId(vcsOrganizationId).build()
        );

        // When
        final ArgumentCaptor<Job> jobArgumentCaptor = ArgumentCaptor.forClass(Job.class);
        when(accountOrganizationStorageAdapter.findOrganizationById(organizationId)).thenReturn(
                organisation
        );
        dataProcessingJobService.startToCollectRepositoriesForOrganizationId(organizationId);

        // Then
        verify(jobManager, times(1)).start(jobArgumentCaptor.capture());
        assertThat(jobArgumentCaptor.getAllValues()).hasSize(1);
        assertThat(jobArgumentCaptor.getAllValues().get(0).getCode()).isEqualTo(CollectRepositoriesJobRunnable.JOB_CODE);
        assertThat(jobArgumentCaptor.getAllValues().get(0).getOrganizationId()).isEqualTo(organisation.getId());
        assertThat(jobArgumentCaptor.getAllValues().get(0).getNextJob()).isNull();
    }

    @Test
    void should_start_to_collect_vcs_data_job_given_an_organization_id_and_team_id() throws SymeoException {
        // Given
        final JobManager jobManager = mock(JobManager.class);
        final VcsService vcsService = mock(VcsService.class);
        final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter =
                mock(AccountOrganizationStorageAdapter.class);
        final RepositoryService repositoryService = mock(RepositoryService.class);
        final SymeoJobApiAdapter symeoJobApiAdapter = mock(SymeoJobApiAdapter.class);
        final OrganizationSettingsService organizationSettingsService = mock(OrganizationSettingsService.class);
        final JobStorage jobStorage = mock(JobStorage.class);
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final DataProcessingJobService dataProcessingJobService =
                new DataProcessingJobService(vcsService,
                        accountOrganizationStorageAdapter, repositoryService, jobManager, symeoJobApiAdapter,
                        organizationSettingsService, expositionStorageAdapter, jobStorage);
        final UUID teamId = UUID.randomUUID();
        final UUID organizationId = UUID.randomUUID();
        final List<Repository> repositories = List.of(
                Repository.builder().id(faker.dragonBall().character()).build(),
                Repository.builder().id(faker.rickAndMorty().character()).build()
        );

        // When
        when(accountOrganizationStorageAdapter.findOrganizationById(organizationId))
                .thenReturn(Organization.builder().id(organizationId).build());
        when(expositionStorageAdapter.findAllRepositoriesForOrganizationIdAndTeamId(organizationId, teamId))
                .thenReturn(
                        repositories
                );
        dataProcessingJobService.startToCollectVcsDataForOrganizationIdAndTeamId(organizationId, teamId);

        // Then
        final ArgumentCaptor<Job> jobArgumentCaptor = ArgumentCaptor.forClass(Job.class);
        verify(jobManager, times(1)).start(jobArgumentCaptor.capture());
        final Job job = jobArgumentCaptor.getValue();
        assertThat(job.getCode()).isEqualTo(CollectVcsDataForOrganizationJobRunnable.JOB_CODE);
        assertThat(job.getOrganizationId()).isEqualTo(organizationId);
        assertThat(job.getTeamId()).isEqualTo(teamId);
    }

    @Test
    void should_start_to_collect_vcs_data_given_an_organization_id() throws SymeoException {
        // Given
        final JobManager jobManager = mock(JobManager.class);
        final VcsService vcsService = mock(VcsService.class);
        final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter =
                mock(AccountOrganizationStorageAdapter.class);
        final RepositoryService repositoryService = mock(RepositoryService.class);
        final SymeoJobApiAdapter symeoJobApiAdapter = mock(SymeoJobApiAdapter.class);
        final OrganizationSettingsService organizationSettingsService = mock(OrganizationSettingsService.class);
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final JobStorage jobStorage = mock(JobStorage.class);
        final DataProcessingJobService dataProcessingJobService =
                new DataProcessingJobService(vcsService,
                        accountOrganizationStorageAdapter, repositoryService, jobManager, symeoJobApiAdapter,
                        organizationSettingsService, expositionStorageAdapter, jobStorage);
        final UUID organizationId = UUID.randomUUID();
        final List<Repository> repositories = List.of(
                Repository.builder().id(faker.dragonBall().character()).build(),
                Repository.builder().id(faker.rickAndMorty().character()).build()
        );

        // When
        when(accountOrganizationStorageAdapter.findOrganizationById(organizationId))
                .thenReturn(Organization.builder().id(organizationId).build());
        when(expositionStorageAdapter.findAllRepositoriesLinkedToTeamsForOrganizationId(organizationId))
                .thenReturn(
                        repositories
                );
        dataProcessingJobService.startToCollectVcsDataForOrganizationId(organizationId);

        // Then
        final ArgumentCaptor<Job> jobArgumentCaptor = ArgumentCaptor.forClass(Job.class);
        verify(jobManager, times(1)).start(jobArgumentCaptor.capture());
        final List<Job> allValues = jobArgumentCaptor.getAllValues();
        final Job job1 = allValues.get(0);
        assertThat(job1.getCode()).isEqualTo(CollectRepositoriesJobRunnable.JOB_CODE);
        assertThat(job1.getOrganizationId()).isEqualTo(organizationId);
        assertThat(job1.getNextJob().getCode()).isEqualTo(CollectVcsDataForOrganizationJobRunnable.JOB_CODE);
        assertThat(job1.getNextJob().getOrganizationId()).isEqualTo(organizationId);
    }

    @Test
    void should_start_all_data_collection_jobs() throws SymeoException {
        // Given
        final JobManager jobManager = mock(JobManager.class);
        final VcsService vcsService = mock(VcsService.class);
        final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter =
                mock(AccountOrganizationStorageAdapter.class);
        final RepositoryService repositoryService = mock(RepositoryService.class);
        final SymeoJobApiAdapter symeoJobApiAdapter = mock(SymeoJobApiAdapter.class);
        final OrganizationSettingsService organizationSettingsService = mock(OrganizationSettingsService.class);
        final JobStorage jobStorage = mock(JobStorage.class);
        final DataProcessingJobService dataProcessingJobService =
                new DataProcessingJobService(vcsService,
                        accountOrganizationStorageAdapter, repositoryService, jobManager, symeoJobApiAdapter,
                        organizationSettingsService, mock(ExpositionStorageAdapter.class), jobStorage);
        final List<Organization> organizations = List.of(
                Organization.builder().id(UUID.randomUUID()).build(),
                Organization.builder().id(UUID.randomUUID()).build()
        );

        // When
        when(accountOrganizationStorageAdapter.findAllOrganization())
                .thenReturn(organizations);
        dataProcessingJobService.startAll();

        // Then
        verify(symeoJobApiAdapter, times(1)).startJobForOrganizationId(organizations.get(0).getId());
        verify(symeoJobApiAdapter, times(1)).startJobForOrganizationId(organizations.get(1).getId());
    }
}
