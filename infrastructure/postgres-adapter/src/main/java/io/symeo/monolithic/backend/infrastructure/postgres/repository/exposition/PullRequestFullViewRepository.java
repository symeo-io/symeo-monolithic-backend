package io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.dto.PullRequestFullViewDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface PullRequestFullViewRepository extends JpaRepository<PullRequestFullViewDTO, String> {

    @Query(value = "select pr.id," +
            "       pr.deleted_line_number," +
            "       pr.added_line_number," +
            "       pr.creation_date," +
            "       pr.merge_date," +
            "       pr.close_date," +
            "       pr.state," +
            "       pr.vcs_url," +
            "       pr.branch_name," +
            "       pr.title," +
            "       pr.commit_number," +
            "       pr.vcs_repository," +
            "       pr.author_login" +
            " from exposition_storage.pull_request pr" +
            " where (pr.merge_date is null and pr.creation_date <= :endDate)" +
            "   or (pr.merge_date >= :startDate)" +
            "    and pr.vcs_repository_id in (select ttr.repository_id" +
            "                                 from exposition_storage.team_to_repository ttr" +
            "                                 where ttr.team_id = :teamId)" +
            " order by pr.technical_modification_date" +
            " limit :end offset :start",
            nativeQuery = true)
    List<PullRequestFullViewDTO> findAllByTeamIdAndStartEndAndEndDateAndPagination(@Param("teamId") final UUID teamId,
                                                                                   @Param("startDate") final Date startDate,
                                                                                   @Param("endDate") final Date endDate,
                                                                                   @Param("start") int start,
                                                                                   @Param("end") int end);

    @Query(value = "select count(pr.id)" +
            " from exposition_storage.pull_request pr" +
            " where (pr.merge_date is null and pr.creation_date <= :endDate)" +
            "   or (pr.merge_date >= :startDate)" +
            "    and pr.vcs_repository_id in (select ttr.repository_id" +
            "                                 from exposition_storage.team_to_repository ttr" +
            "                                 where ttr.team_id = :teamId)"
            , nativeQuery = true)
    int countByTeamIdAndStartEndAndEndDate(@Param("teamId") final UUID teamId, @Param("startDate") final Date startDate,
                                           @Param("endDate") final Date endDate);


}
