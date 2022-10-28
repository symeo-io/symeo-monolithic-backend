package io.symeo.monolithic.backend.infrastructure.postgres.repository.account;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<TeamEntity, UUID>, JpaSpecificationExecutor<TeamEntity> {

    List<TeamEntity> findAllByOrganizationId(UUID organizationId);
    Optional<TeamEntity> findOneById(UUID teamId);
}
