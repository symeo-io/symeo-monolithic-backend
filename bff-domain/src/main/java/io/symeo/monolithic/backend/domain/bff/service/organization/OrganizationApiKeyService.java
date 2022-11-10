package io.symeo.monolithic.backend.domain.bff.service.organization;

import io.symeo.monolithic.backend.domain.bff.model.account.OrganizationApiKey;
import io.symeo.monolithic.backend.domain.bff.port.in.OrganizationApiKeyFacadeAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.OrganizationApiKeyStorageAdapter;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class OrganizationApiKeyService implements OrganizationApiKeyFacadeAdapter {
    private final OrganizationApiKeyStorageAdapter organizationApiKeyStorageAdapter;

    @Override
    public OrganizationApiKey createApiKeyForOrganizationIdAndName(UUID organizationId, String name) throws SymeoException {
        OrganizationApiKey apiKey = new OrganizationApiKey(organizationId, name);
        this.organizationApiKeyStorageAdapter.save(apiKey);

        return apiKey;
    }

    @Override
    public List<OrganizationApiKey> getOrganizationApiKeys(UUID organizationId) throws SymeoException {
        return this.organizationApiKeyStorageAdapter.findByOrganizationId(organizationId);
    }

    @Override
    public void deleteApiKeyForOrganizationId(UUID apiKeyId, UUID organizationId) throws SymeoException {
        this.organizationApiKeyStorageAdapter.deleteForOrganizationId(apiKeyId, organizationId);
    }
}
