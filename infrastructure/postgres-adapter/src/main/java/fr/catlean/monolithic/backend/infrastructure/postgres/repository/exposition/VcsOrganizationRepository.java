package fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition;

import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.VcsOrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VcsOrganizationRepository extends JpaRepository<VcsOrganizationEntity, Long> {

    Optional<VcsOrganizationEntity> findByExternalId(String externalId);

    Optional<VcsOrganizationEntity> findByName(String name);
}
