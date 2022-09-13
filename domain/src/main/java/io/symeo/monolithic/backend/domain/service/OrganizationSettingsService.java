package io.symeo.monolithic.backend.domain.service;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.port.in.OrganizationSettingsFacade;
import io.symeo.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public class OrganizationSettingsService implements OrganizationSettingsFacade {

    private final ExpositionStorageAdapter expositionStorageAdapter;
    private final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter;

    public void initializeOrganizationSettingsForOrganization(final Organization organization) throws SymeoException {
        final Optional<OrganizationSettings> optionalOrganizationSettings =
                accountOrganizationStorageAdapter.findOrganizationSettingsForOrganizationId(organization.getId());
        if (optionalOrganizationSettings.isEmpty()) {
            final String defaultMostUsedBranch =
                    expositionStorageAdapter.findDefaultMostUsedBranchForOrganizationId(organization.getId());
            final OrganizationSettings organizationSettings =
                    OrganizationSettings.initializeFromOrganizationIdAndDefaultBranch(organization.getId(),
                            defaultMostUsedBranch);
            accountOrganizationStorageAdapter.saveOrganizationSettings(organizationSettings);
        }
    }

    @Override
    public OrganizationSettings getOrganizationSettingsForOrganization(final Organization organization) throws SymeoException {
        return accountOrganizationStorageAdapter.findOrganizationSettingsForOrganizationId(organization.getId())
                .orElseThrow(() ->
                        SymeoException.builder()
                                .code(SymeoExceptionCode.ORGANIZATION_SETTINGS_NOT_FOUND)
                                .message(String.format("OrganizationSettings not found for organizationId %s",
                                        organization.getId()))
                                .build()
                );
    }
}
