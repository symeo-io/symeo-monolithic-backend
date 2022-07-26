package fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition;

import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.PullRequestEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.dto.PullRequestTimeToMergeDTO;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PullRequestRepository extends JpaRepository<PullRequestEntity, String>,
        JpaSpecificationExecutor<PullRequestEntity> {

    List<PullRequestEntity> findAllByOrganizationId(UUID organizationId);

    @EntityGraph(value = "PullRequestEntity.TimeToMerge")
    List<PullRequestTimeToMergeDTO> findTimeToMergeDTOsByOrganizationId(@Param("organizationId") UUID organizationId);
}
