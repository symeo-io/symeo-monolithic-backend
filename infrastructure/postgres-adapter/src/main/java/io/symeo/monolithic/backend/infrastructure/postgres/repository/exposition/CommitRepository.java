package io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.CommitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface CommitRepository extends JpaRepository<CommitEntity, String> {

    @Query(nativeQuery = true, value = "select * from exposition_storage.commit c" +
            "    where c.date >= :startDate" +
            "    and c.repository_id in (select ttr.repository_id" +
            "                                 from exposition_storage.team_to_repository ttr" +
            "                                 where ttr.team_id = :teamId)")
    List<CommitEntity> findAllByTeamIdFromStartDate(@Param("teamId") UUID teamId, @Param("startDate") Date startDate);
}
