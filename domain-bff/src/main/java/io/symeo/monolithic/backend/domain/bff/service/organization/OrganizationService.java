package io.symeo.monolithic.backend.domain.bff.service.organization;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.port.in.OrganizationFacadeAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.OrganizationStorageAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.SymeoJobApiAdapter;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OrganizationService implements OrganizationFacadeAdapter {
    private final OrganizationStorageAdapter organizationStorageAdapter;
    private final SymeoJobApiAdapter symeoJobApiAdapter;

    @Override
    public Organization createOrganization(Organization organization) throws SymeoException {
        final Organization createdOrganization = organizationStorageAdapter.createOrganization(organization);
        symeoJobApiAdapter.startJobForOrganizationId(createdOrganization.getId());
        return createdOrganization;
    }
}