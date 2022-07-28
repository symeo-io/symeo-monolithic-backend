package fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition;

import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.PullRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface PullRequestRepository extends JpaRepository<PullRequestEntity, String>,
        JpaSpecificationExecutor<PullRequestEntity> {

    List<PullRequestEntity> findAllByOrganizationId(UUID organizationId);

}
