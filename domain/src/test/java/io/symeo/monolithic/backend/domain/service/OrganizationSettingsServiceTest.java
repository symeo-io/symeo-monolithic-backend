package io.symeo.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class OrganizationSettingsServiceTest {
    private final static Faker faker = new Faker();

    @Test
    void should_initialize_organization_settings_with_deploy_detection_default_most_used_branch_given_no_existing_settings() {
        //
        final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter =
                mock(AccountOrganizationStorageAdapter.class);
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final OrganizationSettingsService organizationSettingsService =
                new OrganizationSettingsService(expositionStorageAdapter, accountOrganizationStorageAdapter);
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .build();
        final String defaultMostUsedBranch = faker.rickAndMorty().character();

        // When
        when(accountOrganizationStorageAdapter.findOrganizationSettingsForOrganizationId(organization.getId()))
                .thenReturn(Optional.empty());
        when(expositionStorageAdapter
                .findDefaultMostUsedBranchForOrganizationId(organization.getId()))
                .thenReturn(defaultMostUsedBranch);
        organizationSettingsService.initializeOrganizationSettingsForOrganization(organization);

        // Then
        verify(accountOrganizationStorageAdapter, times(1))
                .saveOrganizationSettings(
                        OrganizationSettings.
                                initializeFromDefaultBranch(defaultMostUsedBranch)
                );
    }

    @Test
    void should_not_initialize_organization_settings_for_existing_settings() {
        // Given
        final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter =
                mock(AccountOrganizationStorageAdapter.class);
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final OrganizationSettingsService organizationSettingsService =
                new OrganizationSettingsService(expositionStorageAdapter, accountOrganizationStorageAdapter);
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .build();

        // When
        when(accountOrganizationStorageAdapter.findOrganizationSettingsForOrganizationId(organization.getId()))
                .thenReturn(Optional.ofNullable(OrganizationSettings.initializeFromDefaultBranch(faker.rickAndMorty().character())));
        organizationSettingsService.initializeOrganizationSettingsForOrganization(organization);

        // Then
        verifyNoInteractions(expositionStorageAdapter);
        verify(accountOrganizationStorageAdapter, times(0)).saveOrganizationSettings(any());
    }

    @Test
    void should_find_organization_settings_given_an_organization_id() throws SymeoException {
        // Given
        final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter =
                mock(AccountOrganizationStorageAdapter.class);
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final OrganizationSettingsService organizationSettingsService =
                new OrganizationSettingsService(expositionStorageAdapter, accountOrganizationStorageAdapter);
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .build();
        final Optional<OrganizationSettings> optionalOrganizationSettings =
                Optional.of(OrganizationSettings.initializeFromDefaultBranch(faker.rickAndMorty().character()));

        // When
        when(accountOrganizationStorageAdapter.findOrganizationSettingsForOrganizationId(organization.getId()))
                .thenReturn(optionalOrganizationSettings);
        final OrganizationSettings organizationSettings =
                organizationSettingsService.findForOrganizationId(organization);

        // Then
        assertThat(organizationSettings).isEqualTo(optionalOrganizationSettings.get());
    }

    @Test
    void should_raise_functional_not_found_exception() {
        // Given
        final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter =
                mock(AccountOrganizationStorageAdapter.class);
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final OrganizationSettingsService organizationSettingsService =
                new OrganizationSettingsService(expositionStorageAdapter, accountOrganizationStorageAdapter);
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .build();

        // When
        SymeoException expectedSymeoException = null;
        when(accountOrganizationStorageAdapter.findOrganizationSettingsForOrganizationId(organization.getId()))
                .thenReturn(Optional.empty());
        try {
            organizationSettingsService.findForOrganizationId(organization);
        } catch (SymeoException symeoException) {
            expectedSymeoException = symeoException;
        }

        // Then
        assertThat(expectedSymeoException).isNotNull();
        assertThat(expectedSymeoException.getCode()).isEqualTo(SymeoExceptionCode.ORGANIZATION_SETTINGS_NOT_FOUND);
        assertThat(expectedSymeoException.getMessage()).isEqualTo(String.format("OrganizationSettings not found for " +
                "organizationId %s", organization.getId()));
    }
}
