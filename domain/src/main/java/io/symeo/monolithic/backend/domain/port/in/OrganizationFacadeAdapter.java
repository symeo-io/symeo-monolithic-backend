package io.symeo.monolithic.backend.domain.port.in;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;

public interface OrganizationFacadeAdapter {

    Organization createOrganization(final Organization organization) throws SymeoException;
}
