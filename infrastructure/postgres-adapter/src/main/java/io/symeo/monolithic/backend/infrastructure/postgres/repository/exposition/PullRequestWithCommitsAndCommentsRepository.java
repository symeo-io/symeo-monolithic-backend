package io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.dto.PullRequestWithCommitsAndCommentsDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface PullRequestWithCommitsAndCommentsRepository
        extends JpaRepository<PullRequestWithCommitsAndCommentsDTO, String> {


    @Query(nativeQuery = true, value = "select pr.id," +
            "       pr.creation_date," +
            "       pr.merge_date," +
            "       pr.state," +
            "       pr.vcs_url," +
            "       pr.branch_name" +
            " from exposition_storage.pull_request pr" +
            " where pr.state = 'merge' " +
            " and ((pr.merge_date is null and pr.creation_date <= :endDate)" +
            " or (pr.merge_date >= :startDate and pr.merge_date <= :endDate))" +
            " and pr.vcs_repository_id in (select ttr.repository_id" +
            "                                 from exposition_storage.team_to_repository ttr" +
            "                                 where ttr.team_id = :teamId)")
    List<PullRequestWithCommitsAndCommentsDTO> findAllMergedByTeamIdForStartDateAndEndDate(
            @Param("teamId") UUID teamId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

}
