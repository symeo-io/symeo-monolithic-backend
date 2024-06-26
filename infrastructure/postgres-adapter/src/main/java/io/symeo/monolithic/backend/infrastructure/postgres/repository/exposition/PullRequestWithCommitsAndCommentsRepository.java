package io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.dto.PullRequestWithCommentsDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface PullRequestWithCommitsAndCommentsRepository
        extends JpaRepository<PullRequestWithCommentsDTO, String> {


    @Query(nativeQuery = true, value = "select pr.id," +
            "       pr.creation_date," +
            "       pr.merge_date," +
            "       pr.state," +
            "       pr.vcs_url," +
            "       pr.merge_commit_sha," +
            "       pr.head," +
            "       pr.base" +
            " from exposition_storage.pull_request pr" +
            " where pr.state in ('merge','open') " +
            " and pr.creation_date < :endDate " +
            " and pr.vcs_repository_id in (select ttr.repository_id" +
            "                                 from exposition_storage.team_to_repository ttr" +
            "                                 where ttr.team_id = :teamId)")
    List<PullRequestWithCommentsDTO> findAllByTeamIdUntilEndDate(
            @Param("teamId") UUID teamId,
            @Param("endDate") Date endDate
    );

}
