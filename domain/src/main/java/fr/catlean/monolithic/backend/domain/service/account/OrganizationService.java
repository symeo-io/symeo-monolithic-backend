package fr.catlean.monolithic.backend.domain.service.account;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.port.in.DataProcessingJobAdapter;
import fr.catlean.monolithic.backend.domain.port.in.OrganizationFacadeAdapter;
import fr.catlean.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OrganizationService implements OrganizationFacadeAdapter {
    private final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter;
    private final DataProcessingJobAdapter dataProcessingJobAdapter;

    @Override
    public Organization createOrganization(Organization organization) throws CatleanException {
        final Organization createdOrganization = accountOrganizationStorageAdapter.createOrganization(organization);
        dataProcessingJobAdapter.start(organization.getVcsOrganization().getName());
        return createdOrganization;
    }
}
