package io.symeo.monolithic.backend.domain.bff.port.out;

import io.symeo.monolithic.backend.domain.bff.model.account.OrganizationApiKey;
import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationApiKeyStorageAdapter {
    Optional<OrganizationApiKey> findOneByKey(String key) throws SymeoException;
    void save(OrganizationApiKey apiKey) throws SymeoException;

    List<OrganizationApiKey> findByOrganizationId(UUID organizationId) throws SymeoException;

    void deleteForOrganizationId(UUID apiKeyId, UUID organizationId) throws SymeoException;
}
