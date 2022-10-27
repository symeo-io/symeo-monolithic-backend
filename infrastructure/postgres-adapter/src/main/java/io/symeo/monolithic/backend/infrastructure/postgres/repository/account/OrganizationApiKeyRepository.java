package io.symeo.monolithic.backend.infrastructure.postgres.repository.account;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationApiKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrganizationApiKeyRepository extends JpaRepository<OrganizationApiKeyEntity, UUID> {
    OrganizationApiKeyEntity findOneByKey(String key);
}
