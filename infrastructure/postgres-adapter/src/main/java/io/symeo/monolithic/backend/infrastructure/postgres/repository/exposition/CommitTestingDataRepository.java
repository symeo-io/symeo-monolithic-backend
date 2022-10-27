package io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition;

import io.symeo.monolithic.backend.job.domain.model.testing.CommitTestingData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitTestingDataRepository extends JpaRepository<CommitTestingData, String> {
}
