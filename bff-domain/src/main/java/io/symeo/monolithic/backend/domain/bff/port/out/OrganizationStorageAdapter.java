package io.symeo.monolithic.backend.domain.bff.port.out;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationStorageAdapter {
    Optional<Organization> findOrganizationById(UUID organizationId) throws SymeoException;

    Organization createOrganization(Organization organization) throws SymeoException;

    void saveOrganizationSettings(OrganizationSettings organizationSettings) throws SymeoException;

    Optional<OrganizationSettings> findOrganizationSettingsForOrganizationId(UUID organizationId) throws SymeoException;

    Optional<OrganizationSettings> findOrganizationSettingsForIdAndOrganizationId(UUID organizationSettingsId,
                                                                                  UUID organizationId) throws SymeoException;
}
