package io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.dto.PullRequestSizeDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.CustomPullRequestViewRepository.ACTIVE_PULL_REQUEST_SQL_FILTERS;

public interface PullRequestSizeRepository extends JpaRepository<PullRequestSizeDTO, String> {

    @Query(nativeQuery = true,
            value = " select pr.id, pr.deleted_line_number, pr.added_line_number," +
                    " pr.creation_date, pr.merge_date, pr.state, pr.close_date," +
                    " pr.vcs_url, " + "pr.head," +
                    "       pr.base" +
                    " from exposition_storage.pull_request pr" +
                    " where pr.organization_id = :organizationId " +
                    " and (" +
                    ACTIVE_PULL_REQUEST_SQL_FILTERS +
                    "      )" +
                    " and pr.vcs_repository_id in (" +
                    "    select ttr.repository_id from exposition_storage.team_to_repository ttr" +
                    "    where ttr.team_id = :teamId ) order by pr.creation_date")
    List<PullRequestSizeDTO> findPullRequestSizeDTOsByOrganizationIdAndTeamIdForBetweenStartDateAndEndDate(@Param(
            "organizationId") UUID organizationId,
                                                                                                           @Param("teamId") UUID teamId,
                                                                                                           @Param("startDate") Date startDate,
                                                                                                           @Param("endDate") Date endDate);


}
