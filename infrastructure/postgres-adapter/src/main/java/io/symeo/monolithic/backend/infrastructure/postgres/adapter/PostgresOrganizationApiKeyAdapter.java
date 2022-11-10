package io.symeo.monolithic.backend.infrastructure.postgres.adapter;

import io.symeo.monolithic.backend.domain.bff.model.account.OrganizationApiKey;
import io.symeo.monolithic.backend.domain.bff.port.out.OrganizationApiKeyStorageAdapter;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.OrganizationApiKeyMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationApiKeyRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.POSTGRES_EXCEPTION;
import static io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.OrganizationApiKeyMapper.domainToEntity;

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

    @Override
    @Transactional(readOnly = true)
    public void save(OrganizationApiKey apiKey) throws SymeoException {
        try {
            this.organizationApiKeyRepository.save(domainToEntity(apiKey));
        } catch (Exception e) {
            final String message = "Failed to save api key";
            LOGGER.error(message);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationApiKey> findByOrganizationId(UUID organizationId) throws SymeoException {
        try {
            return this.organizationApiKeyRepository.findByOrganizationId(organizationId).stream()
                    .map(OrganizationApiKeyMapper::entityToDomain).toList();
        } catch (Exception e) {
            final String message = "Failed to fetch api keys";
            LOGGER.error(message);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void deleteForOrganizationId(UUID apiKeyId, UUID organizationId) throws SymeoException {
        try {
            this.organizationApiKeyRepository.deleteByIdAndOrganizationId(apiKeyId, organizationId);
        } catch (Exception e) {
            final String message = "Failed to fetch api keys";
            LOGGER.error(message);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }
}
