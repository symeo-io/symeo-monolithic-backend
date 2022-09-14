package io.symeo.monolithic.backend.domain.service.account;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.port.in.DataProcessingJobAdapter;
import io.symeo.monolithic.backend.domain.port.in.OrganizationFacadeAdapter;
import io.symeo.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OrganizationService implements OrganizationFacadeAdapter {
    private final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter;
    private final DataProcessingJobAdapter dataProcessingJobAdapter;

    @Override
    public Organization createOrganization(Organization organization) throws SymeoException {
        final Organization createdOrganization = accountOrganizationStorageAdapter.createOrganization(organization);
        dataProcessingJobAdapter.startToCollectRepositoriesForOrganizationId(createdOrganization.getId());
        return createdOrganization;
    }
}
