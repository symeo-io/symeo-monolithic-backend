package fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition;

import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.PullRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PullRequestRepository extends JpaRepository<PullRequestEntity, String>,
        JpaSpecificationExecutor<PullRequestEntity> {

    List<PullRequestEntity> findAllByOrganizationId(String organizationId);
}
