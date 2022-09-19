package io.symeo.monolithic.backend.domain.job.runnable;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import io.symeo.monolithic.backend.domain.port.out.JobStorage;
import io.symeo.monolithic.backend.domain.service.OrganizationSettingsService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class InitializeOrganizationSettingsJobRunnableTest {

    @Test
    void should_initialize_organization_settings() throws SymeoException {
        // Given
        final Organization organization1 = Organization.builder().id(UUID.randomUUID()).build();
        final OrganizationSettingsService organizationSettingsService = mock(OrganizationSettingsService.class);
        final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter =
                mock(AccountOrganizationStorageAdapter.class);
        final InitializeOrganizationSettingsJobRunnable initializeOrganizationSettingsJobRunnable =
                InitializeOrganizationSettingsJobRunnable.builder()
                        .organizationSettingsService(organizationSettingsService)
                        .accountOrganizationStorageAdapter(accountOrganizationStorageAdapter)
                        .organizationId(organization1.getId())
                        .jobStorage(mock(JobStorage.class))
                        .build();

        // When
        when(accountOrganizationStorageAdapter.findOrganizationById(organization1.getId()))
                .thenReturn(organization1);
        initializeOrganizationSettingsJobRunnable.initializeTasks();
        initializeOrganizationSettingsJobRunnable.run(6L);

        // Then
        verify(organizationSettingsService, times(1)).initializeOrganizationSettingsForOrganization(organization1);
        assertThat(initializeOrganizationSettingsJobRunnable.getCode()).isEqualTo(InitializeOrganizationSettingsJobRunnable.JOB_CODE);
    }
}
