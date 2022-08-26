package io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.CommitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitRepository extends JpaRepository<CommitEntity, String> {
}
