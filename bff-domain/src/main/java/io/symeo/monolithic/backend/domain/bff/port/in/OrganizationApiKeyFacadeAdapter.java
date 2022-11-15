package io.symeo.monolithic.backend.domain.bff.port.in;
import io.symeo.monolithic.backend.domain.bff.model.account.OrganizationApiKey;
import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.util.List;
import java.util.UUID;

public interface OrganizationApiKeyFacadeAdapter {
    OrganizationApiKey createApiKeyForOrganizationIdAndName(UUID organizationId, String name) throws SymeoException;
    List<OrganizationApiKey> getOrganizationApiKeys(UUID organizationId) throws SymeoException;
    void deleteApiKeyForOrganizationId(UUID apiKeyId, UUID organizationId) throws SymeoException;
}
