package io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.VcsOrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VcsOrganizationRepository extends JpaRepository<VcsOrganizationEntity, Long> {

    Optional<VcsOrganizationEntity> findByExternalId(String externalId);

    Optional<VcsOrganizationEntity> findByName(String name);

    Optional<VcsOrganizationEntity> findByOrganizationId(UUID organizationId);
}
