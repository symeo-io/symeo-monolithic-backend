package io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.CommitTestingDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

public interface CommitTestingDataRepository extends JpaRepository<CommitTestingDataEntity, UUID> {
    Optional<CommitTestingDataEntity> findTopByOrganizationIdAndRepositoryNameAndBranchNameAndDateBeforeOrderByDateDesc(
            UUID organizationId,
            String repositoryName,
            String branchName,
            ZonedDateTime date
    );
}
