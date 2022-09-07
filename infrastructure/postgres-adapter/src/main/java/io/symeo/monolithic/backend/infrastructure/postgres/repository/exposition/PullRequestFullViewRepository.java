package io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.dto.PullRequestFullViewDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.UUID;

public interface PullRequestFullViewRepository extends JpaRepository<PullRequestFullViewDTO, String> {

    @Query(value = "select count(pr.id)" +
            " from exposition_storage.pull_request pr" +
            " where (" +
            "           pr.creation_date < :endDate " +
            "               and (" +
            "                   pr.merge_date >= :startDate or pr.close_date >= :startDate" +
            "                   or (pr.merge_date is null and pr.close_date is null)" +
            "                   )" +
            "      )" +
            "    and pr.vcs_repository_id in (select ttr.repository_id" +
            "                                 from exposition_storage.team_to_repository ttr" +
            "                                 where ttr.team_id = :teamId)"
            , nativeQuery = true)
    int countByTeamIdAndStartEndAndEndDate(@Param("teamId") final UUID teamId, @Param("startDate") final Date startDate,
                                           @Param("endDate") final Date endDate);


}
