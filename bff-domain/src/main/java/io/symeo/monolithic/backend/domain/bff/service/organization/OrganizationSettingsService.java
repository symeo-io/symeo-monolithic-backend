package io.symeo.monolithic.backend.domain.bff.service.organization;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.bff.port.in.OrganizationSettingsFacade;
import io.symeo.monolithic.backend.domain.bff.port.out.BffExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.OrganizationStorageAdapter;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import lombok.AllArgsConstructor;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class OrganizationSettingsService implements OrganizationSettingsFacade {

    private final BffExpositionStorageAdapter bffExpositionStorageAdapter;
    private final OrganizationStorageAdapter organizationStorageAdapter;

    public void initializeOrganizationSettingsForOrganization(final Organization organization) throws SymeoException {
        final Optional<OrganizationSettings> optionalOrganizationSettings =
                organizationStorageAdapter.findOrganizationSettingsForOrganizationId(organization.getId());
        if (optionalOrganizationSettings.isEmpty()) {
            final String defaultMostUsedBranch =
                    bffExpositionStorageAdapter.findDefaultMostUsedBranchForOrganizationId(organization.getId());
            final OrganizationSettings organizationSettings =
                    OrganizationSettings.initializeFromOrganizationIdAndDefaultBranch(organization.getId(),
                            defaultMostUsedBranch);
            organizationStorageAdapter.saveOrganizationSettings(organizationSettings);
        }
    }

    @Override
    public OrganizationSettings getOrganizationSettingsForOrganization(final Organization organization) throws SymeoException {
        return organizationStorageAdapter.findOrganizationSettingsForOrganizationId(organization.getId())
                .orElseThrow(() ->
                        SymeoException.builder()
                                .code(SymeoExceptionCode.ORGANIZATION_SETTINGS_NOT_FOUND)
                                .message(String.format("OrganizationSettings not found for organizationId %s",
                                        organization.getId()))
                                .build()
                );
    }

    @Override
    public void updateOrganizationSettings(final OrganizationSettings organizationSettings) throws SymeoException {
        final Optional<OrganizationSettings> organizationSettingsToUpdate =
                getOrganizationSettingsForIdAndOrganizationId(organizationSettings.getId(),
                        organizationSettings.getOrganizationId());
        if (organizationSettingsToUpdate.isPresent()) {
            organizationStorageAdapter.saveOrganizationSettings(organizationSettings);
        } else {
            throw SymeoException.builder()
                    .code(SymeoExceptionCode.ORGANIZATION_SETTINGS_NOT_FOUND)
                    .message(String.format("OrganizationSettings not found for organizationSettingsId %s or user not " +
                                    "allowed to modify organizationSettings",
                            organizationSettings.getId()))
                    .build();
        }
    }

    @Override
    public Optional<OrganizationSettings> getOrganizationSettingsForIdAndOrganizationId(UUID organizationSettingsId,
                                                                                        UUID organizationId) {
        return organizationStorageAdapter.findOrganizationSettingsForIdAndOrganizationId(organizationSettingsId,
                organizationId);
    }
}
