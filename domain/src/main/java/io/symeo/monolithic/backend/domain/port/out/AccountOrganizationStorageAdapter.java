package io.symeo.monolithic.backend.domain.port.out;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;

import java.util.List;
import java.util.UUID;

public interface AccountOrganizationStorageAdapter {
    Organization findOrganizationById(UUID organizationId) throws SymeoException;

    Organization createOrganization(Organization organization) throws SymeoException;

    List<Organization> findAllOrganization();
}
