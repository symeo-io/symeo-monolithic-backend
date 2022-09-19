package io.symeo.monolithic.backend.domain.job.runnable;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.port.out.JobStorage;
import io.symeo.monolithic.backend.domain.service.platform.vcs.VcsService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class CollectVcsDataForOrganizationAndTeamJobRunnableTest {

    @Test
    void should_collect_vcs_data_for_organization_and_team() throws SymeoException {
        // Given
        final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter =
                mock(AccountOrganizationStorageAdapter.class);
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
                        .accountOrganizationStorageAdapter(accountOrganizationStorageAdapter)
                        .expositionStorageAdapter(expositionStorageAdapter)
                        .jobStorage(jobStorage)
                        .vcsService(vcsService)
                        .organizationId(organization.getId())
                        .teamId(teamId)
                        .build();

        // When
        when(accountOrganizationStorageAdapter.findOrganizationById(organization.getId()))
                .thenReturn(organization);
        when(expositionStorageAdapter.findAllRepositoriesForOrganizationIdAndTeamId(organization.getId(), teamId))
                .thenReturn(List.of(
                        Repository.builder().id("1L").build(),
                        Repository.builder().id("2L").build()
                ));
        jobRunnable.initializeTasks();
        jobRunnable.run(1L);

        // Then
        verify(vcsService, times(2)).collectVcsDataForOrganizationAndRepository(any(), any());
        verify(jobStorage, times(2)).updateJobWithTasksForJobId(any(), any());
    }
}
