package io.symeo.monolithic.backend.infrastructure.postgres.repository.account;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationApiKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationApiKeyRepository extends JpaRepository<OrganizationApiKeyEntity, UUID> {
    Optional<OrganizationApiKeyEntity> findByKey(String key);
    List<OrganizationApiKeyEntity> findByOrganizationId(UUID organizationId);

    @Transactional
    void deleteByIdAndOrganizationId(UUID id, UUID organizationId);
}
