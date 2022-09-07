package io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.RepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RepositoryRepository extends JpaRepository<RepositoryEntity, String>,
        JpaSpecificationExecutor<RepositoryEntity> {

    List<RepositoryEntity> findRepositoryEntitiesByOrganizationId(UUID organizationId);

    @Query(nativeQuery = true,
            value = "select result.default_branch" +
                    " from (select r.default_branch, count(r.default_branch) count" +
                    "      from exposition_storage.repository r" +
                    "      where r.organization_id = :organizationId" +
                    "      group by r.default_branch" +
                    "      order by count desc" +
                    "      limit 1) as result")
    String findDefaultMostUsedBranchForOrganizationId(@Param("organizationId") UUID organizationId);
}
