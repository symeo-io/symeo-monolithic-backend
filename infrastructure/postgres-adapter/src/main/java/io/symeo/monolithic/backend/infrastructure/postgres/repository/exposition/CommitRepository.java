package io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition;

import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.CommitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface CommitRepository extends JpaRepository<CommitEntity, String> {

    @Query(nativeQuery = true, value = "select * from exposition_storage.commit c" +
            "    left join exposition_storage.commit_to_parent_sha ctps on ctps.sha = c.sha " +
            "    where c.repository_id in (select ttr.repository_id" +
            "                                 from exposition_storage.team_to_repository ttr" +
            "                                 where ttr.team_id = :teamId)")
    List<CommitEntity> findAllByTeamId(@Param("teamId") UUID teamId);

    @Query(nativeQuery = true, value = "select * from exposition_storage.commit c" +
            " where c.sha in :shaList " +
            " and c.date <= :endDate and c.date > :startDate")
    List<Commit> findAllForShaListBetweenStartDateAndEndDate(@Param("shaList") List<String> shaList,
                                                             @Param("startDate") Date startDate,
                                                             @Param("endDate") Date endDate);
}
