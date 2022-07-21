package fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition;

import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface RepositoryRepository extends JpaRepository<RepositoryEntity, Long>,
        JpaSpecificationExecutor<RepositoryEntity> {

    List<RepositoryEntity> findRepositoryEntitiesByOrganizationId(String organizationId);
}
