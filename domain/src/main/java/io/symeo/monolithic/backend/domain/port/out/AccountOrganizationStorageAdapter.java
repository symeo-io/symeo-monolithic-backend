package io.symeo.monolithic.backend.domain.port.out;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;

public interface AccountOrganizationStorageAdapter {
    Organization findVcsOrganizationForName(String vcsOrganizationName) throws SymeoException;

    Organization createOrganization(Organization organization) throws SymeoException;
}
