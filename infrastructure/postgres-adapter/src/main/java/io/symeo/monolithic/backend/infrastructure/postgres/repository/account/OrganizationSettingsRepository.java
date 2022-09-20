package io.symeo.monolithic.backend.infrastructure.postgres.repository.account;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationSettingsRepository extends JpaRepository<OrganizationSettingsEntity, UUID> {

    Optional<OrganizationSettingsEntity> findByOrganizationId(UUID organizationId);

    Optional<OrganizationSettingsEntity> findByIdAndOrganizationId(UUID organizationSettingsId, UUID organizationId);
}
