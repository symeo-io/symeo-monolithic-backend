package io.symeo.monolithic.backend.domain.bff.port.in;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;

import java.util.Optional;

public interface OrganizationFacadeAdapter {

    Organization createOrganization(final Organization organization) throws SymeoException;
    Optional<Organization> getOrganizationForApiKey(String key) throws SymeoException;
}
