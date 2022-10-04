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

    @Query(nativeQuery = true,
            value = "select distinct r.*" +
                    " from exposition_storage.team_to_repository ttr" +
                    " join exposition_storage.repository r on r.id = ttr.repository_id" +
                    " where ttr.team_id = :teamId" +
                    " and r.organization_id = :organizationId")
    List<RepositoryEntity> findAllRepositoriesForOrganizationIdAndTeamId(@Param("organizationId") UUID organizationId,
                                                                         @Param("teamId") UUID teamId);


    @Query(nativeQuery = true,
            value = "select distinct r.*" +
                    " from exposition_storage.team_to_repository ttr" +
                    " join exposition_storage.repository r on r.id = ttr.repository_id" +
                    " and r.organization_id = :organizationId")
    List<RepositoryEntity> findAllRepositoriesLinkedToTeamsForOrganizationId(UUID organizationId);
}
