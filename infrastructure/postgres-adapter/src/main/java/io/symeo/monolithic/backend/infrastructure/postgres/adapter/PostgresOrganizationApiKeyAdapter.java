package io.symeo.monolithic.backend.infrastructure.postgres.adapter;

import io.symeo.monolithic.backend.domain.bff.model.account.OrganizationApiKey;
import io.symeo.monolithic.backend.domain.bff.port.out.OrganizationApiKeyStorageAdapter;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationApiKeyRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.POSTGRES_EXCEPTION;
import static io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.OrganizationApiKeyMapper.entityToDomain;

@AllArgsConstructor
@Slf4j
public class PostgresOrganizationApiKeyAdapter implements OrganizationApiKeyStorageAdapter {
    private final OrganizationApiKeyRepository organizationApiKeyRepository;

    @Override
    public OrganizationApiKey findOneByKey(String key) throws SymeoException {
        try {
            return entityToDomain(this.organizationApiKeyRepository.findOneByKey(key));
        } catch (Exception e) {
            final String message = "Failed to fetch api key";
            LOGGER.error(message);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }
}
