package io.symeo.monolithic.backend.infrastructure.postgres.mapper.account;

import io.symeo.monolithic.backend.domain.bff.model.account.OrganizationApiKey;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationApiKeyEntity;

import java.util.UUID;

import static java.util.Objects.isNull;

public interface OrganizationApiKeyMapper {
    static OrganizationApiKeyEntity domainToEntity(final OrganizationApiKey organizationApiKey) {
        return OrganizationApiKeyEntity.builder()
                .id(isNull(organizationApiKey.getId()) ? UUID.randomUUID() : organizationApiKey.getId())
                .organizationId(organizationApiKey.getOrganizationId())
                .name(organizationApiKey.getName())
                .key(organizationApiKey.getKey())
                .build();
    }

    static OrganizationApiKey entityToDomain(final OrganizationApiKeyEntity organizationApiKeyEntity) {
        return OrganizationApiKey.builder()
                .id(organizationApiKeyEntity.getId())
                .organizationId(organizationApiKeyEntity.getOrganizationId())
                .name(organizationApiKeyEntity.getName())
                .key(organizationApiKeyEntity.getKey())
                .build();
    }
}
