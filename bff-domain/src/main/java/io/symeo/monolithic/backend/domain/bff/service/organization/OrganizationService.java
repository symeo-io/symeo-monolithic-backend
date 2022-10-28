package io.symeo.monolithic.backend.domain.bff.service.organization;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.OrganizationApiKey;
import io.symeo.monolithic.backend.domain.bff.port.in.OrganizationFacadeAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.OrganizationApiKeyStorageAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.OrganizationStorageAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.SymeoDataProcessingJobApiAdapter;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public class OrganizationService implements OrganizationFacadeAdapter {
    private final OrganizationStorageAdapter organizationStorageAdapter;
    private final OrganizationApiKeyStorageAdapter organizationApiKeyStorageAdapter;
    private final SymeoDataProcessingJobApiAdapter symeoDataProcessingJobApiAdapter;

    @Override
    public Organization createOrganization(Organization organization) throws SymeoException {
        final Organization createdOrganization = organizationStorageAdapter.createOrganization(organization);
        symeoDataProcessingJobApiAdapter.startDataProcessingJobForOrganizationIdAndVcsOrganizationId(
                createdOrganization.getId(), createdOrganization.getVcsOrganization().getId()
        );
        return createdOrganization;
    }

    @Override
    public Optional<Organization> getOrganizationForApiKey(String key) throws SymeoException {
        final Optional<OrganizationApiKey> organizationApiKey = this.organizationApiKeyStorageAdapter.findOneByKey(key);
        if (organizationApiKey.isPresent()) {
            return organizationStorageAdapter.findOrganizationById(organizationApiKey.get().getOrganizationId());
        }

        return Optional.empty();
    }
}
