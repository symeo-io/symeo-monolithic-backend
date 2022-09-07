package io.symeo.monolithic.backend.domain.port.in;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.account.settings.OrganizationSettings;

public interface OrganizationSettingsFacade {
    OrganizationSettings getOrganizationSettingsForOrganization(Organization organization) throws SymeoException;
}
