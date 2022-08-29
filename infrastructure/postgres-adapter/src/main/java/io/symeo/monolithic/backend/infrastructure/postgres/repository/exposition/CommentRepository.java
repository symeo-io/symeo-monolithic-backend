package io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<CommentEntity, String> {
}
