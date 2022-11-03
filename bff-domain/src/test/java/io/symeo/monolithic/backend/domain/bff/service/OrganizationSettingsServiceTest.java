package io.symeo.monolithic.backend.domain.bff.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.DeliverySettings;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.DeployDetectionSettings;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.bff.port.out.BffExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.OrganizationStorageAdapter;
import io.symeo.monolithic.backend.domain.bff.service.organization.OrganizationSettingsService;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
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
        final OrganizationStorageAdapter organizationStorageAdapter =
                mock(OrganizationStorageAdapter.class);
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final OrganizationSettingsService organizationSettingsService =
                new OrganizationSettingsService(bffExpositionStorageAdapter, organizationStorageAdapter);
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .build();
        final String defaultMostUsedBranch = faker.rickAndMorty().character();

        // When
        when(organizationStorageAdapter.findOrganizationSettingsForOrganizationId(organization.getId()))
                .thenReturn(Optional.empty());
        when(bffExpositionStorageAdapter
                .findDefaultMostUsedBranchForOrganizationId(organization.getId()))
                .thenReturn(defaultMostUsedBranch);
        organizationSettingsService.initializeOrganizationSettingsForOrganization(organization);

        // Then
        verify(organizationStorageAdapter, times(1))
                .saveOrganizationSettings(
                        OrganizationSettings.
                                initializeFromOrganizationId(organization.getId())
                );
    }

    @Test
    void should_not_initialize_organization_settings_for_existing_settings() throws SymeoException {
        // Given
        final OrganizationStorageAdapter organizationStorageAdapter =
                mock(OrganizationStorageAdapter.class);
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final OrganizationSettingsService organizationSettingsService =
                new OrganizationSettingsService(bffExpositionStorageAdapter, organizationStorageAdapter);
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .build();

        // When
        when(organizationStorageAdapter.findOrganizationSettingsForOrganizationId(organization.getId()))
                .thenReturn(Optional.ofNullable(OrganizationSettings.initializeFromOrganizationId(organization.getId())));
        organizationSettingsService.initializeOrganizationSettingsForOrganization(organization);

        // Then
        verifyNoInteractions(bffExpositionStorageAdapter);
        verify(organizationStorageAdapter, times(0)).saveOrganizationSettings(any());
    }

    @Test
    void should_find_organization_settings_given_an_organization_id() throws SymeoException {
        // Given
        final OrganizationStorageAdapter organizationStorageAdapter =
                mock(OrganizationStorageAdapter.class);
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final OrganizationSettingsService organizationSettingsService =
                new OrganizationSettingsService(bffExpositionStorageAdapter, organizationStorageAdapter);
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .build();
        final Optional<OrganizationSettings> optionalOrganizationSettings =
                Optional.of(OrganizationSettings.initializeFromOrganizationId(organization.getId()));

        // When
        when(organizationStorageAdapter.findOrganizationSettingsForOrganizationId(organization.getId()))
                .thenReturn(optionalOrganizationSettings);
        final OrganizationSettings organizationSettings =
                organizationSettingsService.getOrganizationSettingsForOrganization(organization);

        // Then
        assertThat(organizationSettings).isEqualTo(optionalOrganizationSettings.get());
    }

    @Test
    void should_raise_functional_not_found_exception() throws SymeoException {
        // Given
        final OrganizationStorageAdapter organizationStorageAdapter =
                mock(OrganizationStorageAdapter.class);
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final OrganizationSettingsService organizationSettingsService =
                new OrganizationSettingsService(bffExpositionStorageAdapter, organizationStorageAdapter);
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .build();

        // When
        SymeoException expectedSymeoException = null;
        when(organizationStorageAdapter.findOrganizationSettingsForOrganizationId(organization.getId()))
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
        final OrganizationStorageAdapter organizationStorageAdapter =
                mock(OrganizationStorageAdapter.class);
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final OrganizationSettingsService organizationSettingsService =
                new OrganizationSettingsService(bffExpositionStorageAdapter, organizationStorageAdapter);
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
        when(organizationStorageAdapter.findOrganizationSettingsForIdAndOrganizationId(organizationSettingsId,
                organizationId))
                .thenReturn(optionalOrganizationSettings);
        final Optional<OrganizationSettings> organizationSettings =
                organizationSettingsService.getOrganizationSettingsForIdAndOrganizationId(organizationSettingsId,
                        organizationId);

        // Then
        assertThat(organizationSettings).isEqualTo(optionalOrganizationSettings);
    }

    @Test
    void should_update_organization_settings_given_organization_settings() throws SymeoException {
        // Given
        final UUID organizationSettingsId = UUID.randomUUID();
        final OrganizationStorageAdapter organizationStorageAdapter =
                mock(OrganizationStorageAdapter.class);
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final OrganizationSettingsService organizationSettingsService =
                new OrganizationSettingsService(bffExpositionStorageAdapter, organizationStorageAdapter);
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
        organizationStorageAdapter.saveOrganizationSettings(organizationSettings);

        // Then
        verify(organizationStorageAdapter, times(1)).saveOrganizationSettings(organizationSettings);
    }

    @Test
    void should_raise_organization_settings_not_found_exception() throws SymeoException {
        // Given
        final OrganizationStorageAdapter organizationStorageAdapter =
                mock(OrganizationStorageAdapter.class);
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final OrganizationSettingsService organizationSettingsService =
                new OrganizationSettingsService(bffExpositionStorageAdapter, organizationStorageAdapter);
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .build();
        final OrganizationSettings organizationSettings = OrganizationSettings.builder()
                .id(organization.getId())
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
        SymeoException expectedSymeoException = null;
        when(organizationStorageAdapter.findOrganizationSettingsForIdAndOrganizationId(UUID.randomUUID(),
                organization.getId()))
                .thenReturn(Optional.empty());
        try {
            organizationSettingsService.updateOrganizationSettings(organizationSettings);
        } catch (SymeoException symeoException) {
            expectedSymeoException = symeoException;
        }

        // Then
        assertThat(expectedSymeoException).isNotNull();
        assertThat(expectedSymeoException.getCode()).isEqualTo(SymeoExceptionCode.ORGANIZATION_SETTINGS_NOT_FOUND);
        assertThat(expectedSymeoException.getMessage()).isEqualTo(
                String.format("OrganizationSettings not found for organizationSettingsId %s or user not allowed to " +
                        "modify organizationSettings", organizationSettings.getId())
        );

    }
}
