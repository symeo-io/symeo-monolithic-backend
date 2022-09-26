package io.symeo.monolithic.backend.domain.job.runnable;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.helper.DateHelper;
import io.symeo.monolithic.backend.domain.job.Job;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.port.out.OrganizationStorageAdapter;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.port.out.JobStorage;
import io.symeo.monolithic.backend.domain.service.platform.vcs.VcsService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class CollectVcsDataForOrganizationAndTeamJobRunnableTest {

    @Test
    void should_collect_vcs_data_for_organization_and_team_with_a_last_collection_date() throws SymeoException {
        // Given
        final OrganizationStorageAdapter organizationStorageAdapter =
                mock(OrganizationStorageAdapter.class);
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final VcsService vcsService = mock(VcsService.class);
        final UUID teamId = UUID.randomUUID();
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final JobStorage jobStorage = mock(JobStorage.class);
        final Date now = new Date();
        final Date expectedDate = DateHelper.stringToDate("2020-01-01");
        final CollectVcsDataForOrganizationAndTeamJobRunnable jobRunnable =
                CollectVcsDataForOrganizationAndTeamJobRunnable
                        .builder()
                        .organization(
                                organization
                        )
                        .organizationStorageAdapter(organizationStorageAdapter)
                        .expositionStorageAdapter(expositionStorageAdapter)
                        .jobStorage(jobStorage)
                        .vcsService(vcsService)
                        .organizationId(organization.getId())
                        .teamId(teamId)
                        .build();

        // When
        when(organizationStorageAdapter.findOrganizationById(organization.getId()))
                .thenReturn(organization);
        when(expositionStorageAdapter.findAllRepositoriesForOrganizationIdAndTeamId(organization.getId(), teamId))
                .thenReturn(List.of(
                        Repository.builder().id("1L").build(),
                        Repository.builder().id("2L").build()
                ));
        when(jobStorage.findLastJobsForCodeAndOrganizationIdAndLimitAndTeamIdOrderByUpdateDateDesc(
                CollectVcsDataForOrganizationAndTeamJobRunnable.JOB_CODE, organization.getId(), teamId, 10000))
                .thenReturn(List.of(
                        Job.builder().organizationId(organization.getId()).creationDate(now).status(Job.FAILED).build(),
                        Job.builder().organizationId(organization.getId()).creationDate(expectedDate).status(Job.FINISHED).build()
                ));
        jobRunnable.initializeTasks();
        jobRunnable.run(1L);

        // Then
        final ArgumentCaptor<Date> dateArgumentCaptor = ArgumentCaptor.forClass(Date.class);
        verify(vcsService, times(2)).collectVcsDataForOrganizationAndRepositoryFromLastCollectionDate(any(), any(),
                dateArgumentCaptor.capture());
        verify(jobStorage, times(2)).updateJobWithTasksForJobId(any(), any());
        Assertions.assertThat(dateArgumentCaptor.getValue()).isEqualTo(expectedDate);
    }

    @Test
    void should_collect_vcs_data_for_organization_and_team_without_a_last_collection_date() throws SymeoException {
        // Given
        final OrganizationStorageAdapter organizationStorageAdapter =
                mock(OrganizationStorageAdapter.class);
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final VcsService vcsService = mock(VcsService.class);
        final UUID teamId = UUID.randomUUID();
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final JobStorage jobStorage = mock(JobStorage.class);
        final CollectVcsDataForOrganizationAndTeamJobRunnable jobRunnable =
                CollectVcsDataForOrganizationAndTeamJobRunnable
                        .builder()
                        .organization(
                                organization
                        )
                        .organizationStorageAdapter(organizationStorageAdapter)
                        .expositionStorageAdapter(expositionStorageAdapter)
                        .jobStorage(jobStorage)
                        .vcsService(vcsService)
                        .organizationId(organization.getId())
                        .teamId(teamId)
                        .build();

        // When
        when(organizationStorageAdapter.findOrganizationById(organization.getId()))
                .thenReturn(organization);
        when(expositionStorageAdapter.findAllRepositoriesForOrganizationIdAndTeamId(organization.getId(), teamId))
                .thenReturn(List.of(
                        Repository.builder().id("1L").build(),
                        Repository.builder().id("2L").build()
                ));
        when(jobStorage.findLastJobsForCodeAndOrganizationIdAndLimitAndTeamIdOrderByUpdateDateDesc(CollectVcsDataForOrganizationAndTeamJobRunnable.JOB_CODE, organization.getId(), teamId, 1))
                .thenReturn(List.of());
        jobRunnable.initializeTasks();
        jobRunnable.run(1L);

        // Then
        final ArgumentCaptor<Date> dateArgumentCaptor = ArgumentCaptor.forClass(Date.class);
        verify(vcsService, times(2)).collectVcsDataForOrganizationAndRepositoryFromLastCollectionDate(any(), any(),
                dateArgumentCaptor.capture());
        verify(jobStorage, times(2)).updateJobWithTasksForJobId(any(), any());
        Assertions.assertThat(dateArgumentCaptor.getValue()).isNull();
    }


}
