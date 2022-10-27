package io.symeo.monolithic.backend.infrastructure.postgres.adapter;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.bff.model.account.OrganizationApiKey;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationApiKeyEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationApiKeyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class PostgresOrganizationApiKeyAdapterTest {

    private final Faker faker = new Faker();
    @Autowired
    private OrganizationApiKeyRepository organizationApiKeyRepository;
    @Test
    void should_find_by_key_string() throws SymeoException {

        // Given
        final String key = faker.pokemon().name();
        final PostgresOrganizationApiKeyAdapter postgresOrganizationApiKeyAdapter = new PostgresOrganizationApiKeyAdapter(organizationApiKeyRepository);
        final OrganizationApiKeyEntity organizationApiKeyEntity = OrganizationApiKeyEntity.builder()
                .id(UUID.randomUUID())
                .organizationId(UUID.randomUUID())
                .key(key)
                .name(faker.name().firstName())
                .build();

        this.organizationApiKeyRepository.save(organizationApiKeyEntity);

        // When
        final Optional<OrganizationApiKey> result = postgresOrganizationApiKeyAdapter.findOneByKey(key);

        // Then
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId()).isEqualTo(organizationApiKeyEntity.getId());
        assertThat(result.get().getName()).isEqualTo(organizationApiKeyEntity.getName());
        assertThat(result.get().getKey()).isEqualTo(organizationApiKeyEntity.getKey());
        assertThat(result.get().getOrganizationId()).isEqualTo(organizationApiKeyEntity.getOrganizationId());
    }
}
