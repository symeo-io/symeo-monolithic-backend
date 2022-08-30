package io.symeo.monolithic.backend.domain.job.runnable;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.service.OrganizationSettingsService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class InitializeOrganizationSettingsJobRunnableTest {

    @Test
    void should_initialize_organization_settings() throws SymeoException {
        // Given
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final OrganizationSettingsService organizationSettingsService = mock(OrganizationSettingsService.class);
        final InitializeOrganizationSettingsJobRunnable initializeOrganizationSettingsJobRunnable =
                InitializeOrganizationSettingsJobRunnable.builder()
                        .organizationSettingsService(organizationSettingsService)
                        .organization(organization)
                        .build();

        // When
        initializeOrganizationSettingsJobRunnable.run();

        // Then
        verify(organizationSettingsService, times(1)).initializeOrganizationSettingsForOrganization(organization);
        assertThat(initializeOrganizationSettingsJobRunnable.getCode()).isEqualTo(InitializeOrganizationSettingsJobRunnable.JOB_CODE);
    }
}
