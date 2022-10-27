package io.symeo.monolithic.backend.domain.bff.port.in;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;

public interface OrganizationFacadeAdapter {

    Organization createOrganization(final Organization organization) throws SymeoException;
    Organization getOrganizationForApiKey(String key) throws SymeoException;
}
