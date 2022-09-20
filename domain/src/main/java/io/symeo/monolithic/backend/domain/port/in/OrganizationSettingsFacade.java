package io.symeo.monolithic.backend.domain.port.in;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.account.settings.OrganizationSettings;

import java.util.Optional;
import java.util.UUID;
public interface OrganizationSettingsFacade {
    OrganizationSettings getOrganizationSettingsForOrganization(Organization organization) throws SymeoException;

    void updateOrganizationSettings(OrganizationSettings organizationSettings) throws SymeoException;

    Optional<OrganizationSettings> getOrganizationSettingsForIdAndOrganizationId(UUID organizationSettingsId, UUID organizationId) throws SymeoException;
}
