package io.symeo.monolithic.backend.domain.bff.port.out;

import io.symeo.monolithic.backend.domain.bff.model.account.OrganizationApiKey;
import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.util.Optional;

public interface OrganizationApiKeyStorageAdapter {
    Optional<OrganizationApiKey> findOneByKey(String key) throws SymeoException;
}
