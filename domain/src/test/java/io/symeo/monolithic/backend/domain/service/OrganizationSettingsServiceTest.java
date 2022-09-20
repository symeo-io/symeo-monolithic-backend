package io.symeo.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.account.settings.DeliverySettings;
import io.symeo.monolithic.backend.domain.model.account.settings.DeployDetectionSettings;
import io.symeo.monolithic.backend.domain.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.port.in.OrganizationSettingsFacade;
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
    void should_initialize_organization_settings_with_deploy_detection_default_most_used_branch_given_no_existing_settings() throws SymeoException {
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
                                initializeFromOrganizationIdAndDefaultBranch(organization.getId(),
                                        defaultMostUsedBranch)
                );
    }

    @Test
    void should_not_initialize_organization_settings_for_existing_settings() throws SymeoException {
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
                .thenReturn(Optional.ofNullable(OrganizationSettings.initializeFromOrganizationIdAndDefaultBranch(organization.getId(),
                        faker.rickAndMorty().character())));
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
                Optional.of(OrganizationSettings.initializeFromOrganizationIdAndDefaultBranch(organization.getId(),
                        faker.rickAndMorty().character()));

        // When
        when(accountOrganizationStorageAdapter.findOrganizationSettingsForOrganizationId(organization.getId()))
                .thenReturn(optionalOrganizationSettings);
        final OrganizationSettings organizationSettings =
                organizationSettingsService.getOrganizationSettingsForOrganization(organization);

        // Then
        assertThat(organizationSettings).isEqualTo(optionalOrganizationSettings.get());
    }

    @Test
    void should_raise_functional_not_found_exception() throws SymeoException {
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
            organizationSettingsService.getOrganizationSettingsForOrganization(organization);
        } catch (SymeoException symeoException) {
            expectedSymeoException = symeoException;
        }

        // Then
        assertThat(expectedSymeoException).isNotNull();
        assertThat(expectedSymeoException.getCode()).isEqualTo(SymeoExceptionCode.ORGANIZATION_SETTINGS_NOT_FOUND);
        assertThat(expectedSymeoException.getMessage()).isEqualTo(String.format("OrganizationSettings not found for " +
                "organizationId %s", organization.getId()));
    }

    @Test
    void should_get_organization_settings_given_an_organization_settings_id_and_organization_id() throws SymeoException {
        // Given
        final UUID organizationSettingsId = UUID.randomUUID();
        final UUID organizationId = UUID.randomUUID();
        final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter =
                mock(AccountOrganizationStorageAdapter.class);
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final OrganizationSettingsService organizationSettingsService =
                new OrganizationSettingsService(expositionStorageAdapter, accountOrganizationStorageAdapter);
        final Optional<OrganizationSettings> optionalOrganizationSettings = Optional.of(OrganizationSettings.builder()
                .id(organizationSettingsId)
                .organizationId(UUID.randomUUID())
                .deliverySettings(
                        DeliverySettings.builder()
                                .deployDetectionSettings(
                                        DeployDetectionSettings.builder()
                                                .tagRegex(faker.gameOfThrones().dragon())
                                                .pullRequestMergedOnBranchRegex(faker.rickAndMorty().character())
                                                .build()
                                )
                                .build()
                )
                .build());

        // When
        when(accountOrganizationStorageAdapter.findOrganizationSettingsForIdAndOrganizationId(organizationSettingsId, organizationId))
                .thenReturn(optionalOrganizationSettings);
        final Optional<OrganizationSettings> organizationSettings =
                organizationSettingsService.getOrganizationSettingsForIdAndOrganizationId(organizationSettingsId, organizationId);

        // Then
        assertThat(organizationSettings).isEqualTo(optionalOrganizationSettings);
    }

    @Test
    void should_update_organization_settings_given_organization_settings() throws SymeoException {
        // Given
        final UUID organizationSettingsId = UUID.randomUUID();
        final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter =
                mock(AccountOrganizationStorageAdapter.class);
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final OrganizationSettingsService organizationSettingsService =
                new OrganizationSettingsService(expositionStorageAdapter, accountOrganizationStorageAdapter);
        final OrganizationSettings organizationSettings = OrganizationSettings.builder()
                .id(organizationSettingsId)
                .organizationId(UUID.randomUUID())
                .deliverySettings(
                        DeliverySettings.builder()
                                .deployDetectionSettings(
                                        DeployDetectionSettings.builder()
                                                .tagRegex(faker.gameOfThrones().dragon())
                                                .pullRequestMergedOnBranchRegex(faker.rickAndMorty().character())
                                                .build()
                                )
                                .build()
                )
                .build();

        // When
        accountOrganizationStorageAdapter.saveOrganizationSettings(organizationSettings);

        // Then
        verify(accountOrganizationStorageAdapter,times(1)).saveOrganizationSettings(organizationSettings);
    }
}
