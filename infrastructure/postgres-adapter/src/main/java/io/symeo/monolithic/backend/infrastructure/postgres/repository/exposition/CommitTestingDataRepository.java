package io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.CommitTestingDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommitTestingDataRepository extends JpaRepository<CommitTestingDataEntity, UUID> {
}
