package io.symeo.monolithic.backend.infrastructure.postgres.adapter;

import io.symeo.monolithic.backend.domain.bff.model.account.OrganizationApiKey;
import io.symeo.monolithic.backend.domain.bff.port.out.OrganizationApiKeyStorageAdapter;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.OrganizationApiKeyMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationApiKeyRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.POSTGRES_EXCEPTION;

@AllArgsConstructor
@Slf4j
public class PostgresOrganizationApiKeyAdapter implements OrganizationApiKeyStorageAdapter {
    private final OrganizationApiKeyRepository organizationApiKeyRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<OrganizationApiKey> findOneByKey(String key) throws SymeoException {
        try {
            return this.organizationApiKeyRepository.findByKey(key)
                    .map(OrganizationApiKeyMapper::entityToDomain);
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
