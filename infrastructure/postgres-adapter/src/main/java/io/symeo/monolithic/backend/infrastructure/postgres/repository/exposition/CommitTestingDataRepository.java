package io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.CommitTestingDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitTestingDataRepository extends JpaRepository<CommitTestingDataEntity, String> {
}
