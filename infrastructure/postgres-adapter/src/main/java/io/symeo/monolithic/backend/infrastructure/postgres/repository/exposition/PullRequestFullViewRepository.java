package io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition;

import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.PullRequestEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.dto.PullRequestFullViewDTO;
import io.symeo.monolithic.backend.job.domain.model.vcs.PullRequest;
import org.springframework.beans.PropertyValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.CustomPullRequestViewRepository.ACTIVE_PULL_REQUEST_SQL_FILTERS;

public interface PullRequestFullViewRepository extends JpaRepository<PullRequestFullViewDTO, String> {

    @Query(value = "select count(pr.id)" +
            " from exposition_storage.pull_request pr" +
            " where (" +
            ACTIVE_PULL_REQUEST_SQL_FILTERS +
            "      )" +
            "    and pr.vcs_repository_id in (select ttr.repository_id" +
            "                                 from exposition_storage.team_to_repository ttr" +
            "                                 where ttr.team_id = :teamId)"
            , nativeQuery = true)
    int countByTeamIdAndStartEndAndEndDate(@Param("teamId") final UUID teamId, @Param("startDate") final Date startDate,
                                           @Param("endDate") final Date endDate);


    @Query(value = "select pr.id," +
            "       pr.deleted_line_number," +
            "       pr.added_line_number," +
            "       pr.creation_date," +
            "       pr.merge_date," +
            "       pr.close_date," +
            "       pr.state," +
            "       pr.vcs_url," +
            "       pr.head," +
            "       pr.base," +
            "       pr.title," +
            "       pr.commit_number," +
            "       pr.vcs_repository," +
            "       pr.author_login," +
            "       pr.merge_commit_sha" +
            " from exposition_storage.pull_request pr" +
            " where pr.state = 'merge' " +
            " and pr.merge_date <= :endDate and pr.merge_date > :startDate" +
            " and pr.vcs_repository_id in (select ttr.repository_id" +
            "                                 from exposition_storage.team_to_repository ttr" +
            "                                 where ttr.team_id = :teamId)", nativeQuery = true
    )
    List<PullRequestFullViewDTO> findAllMergedPullRequestsForTeamIdBetweenStartDateAndDate(@Param("teamId") UUID teamId,
                                                                                           @Param("startDate") Date startDate,
                                                                                           @Param("endDate") Date endDate);

    @Query(value = "select pr.id," +
            "       pr.deleted_line_number," +
            "       pr.added_line_number," +
            "       pr.creation_date," +
            "       pr.merge_date," +
            "       pr.close_date," +
            "       pr.state," +
            "       pr.vcs_url," +
            "       pr.head," +
            "       pr.base," +
            "       pr.title," +
            "       pr.commit_number," +
            "       pr.vcs_repository," +
            "       pr.author_login," +
            "       pr.merge_commit_sha" +
            " from exposition_storage.pull_request pr" +
            " where pr.state = 'merge' " +
            " and pr.merge_date <= :endDate " +
            " and pr.vcs_repository_id in (select ttr.repository_id" +
            "                                 from exposition_storage.team_to_repository ttr" +
            "                                 where ttr.team_id = :teamId)", nativeQuery = true
    )
    List<PullRequestFullViewDTO> findAllMergedPullRequestsForTeamIdUntilEndDate(@Param("teamId") UUID teamId,
                                                                         @Param("endDate") Date endDate);
}
