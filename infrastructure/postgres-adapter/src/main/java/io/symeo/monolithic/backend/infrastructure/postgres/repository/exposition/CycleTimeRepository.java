package io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.CycleTimeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CycleTimeRepository extends JpaRepository<CycleTimeEntity, String> {

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM exposition_storage.cycle_time" +
            " WHERE pull_request_vcs_repository_id = :repositoryId")
    void deleteAllCycleTimesForRepositoryId(@Param("repositoryId") String repositoryId);
}
