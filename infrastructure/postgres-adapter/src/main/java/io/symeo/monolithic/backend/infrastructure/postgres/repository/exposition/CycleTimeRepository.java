package io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.CycleTimeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CycleTimeRepository extends JpaRepository<CycleTimeEntity, String> {
}
