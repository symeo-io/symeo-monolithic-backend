package io.symeo.monolithic.backend.infrastructure.postgres.repository.account;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<OrganizationEntity, UUID>,
        JpaSpecificationExecutor<OrganizationEntity> {

    Optional<OrganizationEntity> findByName(String name);
}
