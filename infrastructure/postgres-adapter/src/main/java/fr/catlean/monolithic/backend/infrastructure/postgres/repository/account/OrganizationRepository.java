package fr.catlean.monolithic.backend.infrastructure.postgres.repository.account;

import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<OrganizationEntity, String>,
        JpaSpecificationExecutor<OrganizationEntity> {

    Optional<OrganizationEntity> findByExternalId(String externalId);

    Optional<OrganizationEntity> findByName(String name);
}
