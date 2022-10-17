package io.symeo.monolithic.backend.domain.service.account;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.port.in.DataProcessingJobAdapter;
import io.symeo.monolithic.backend.domain.port.in.OrganizationFacadeAdapter;
import io.symeo.monolithic.backend.domain.port.out.OrganizationStorageAdapter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OrganizationService implements OrganizationFacadeAdapter {
    private final OrganizationStorageAdapter organizationStorageAdapter;
    private final DataProcessingJobAdapter dataProcessingJobAdapter;

    @Override
    public Organization createOrganization(Organization organization) throws SymeoException {
        final Organization createdOrganization = organizationStorageAdapter.createOrganization(organization);
        dataProcessingJobAdapter.startToCollectRepositoriesForOrganizationId(createdOrganization.getId());
        return createdOrganization;
    }
}
