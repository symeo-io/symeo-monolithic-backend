package fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition;

import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.dto.PullRequestTimeToMergeDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PullRequestTimeToMergeRepository extends JpaRepository<PullRequestTimeToMergeDTO, String> {

    @Query(nativeQuery = true,
            value = " select pr.id, pr.days_opened, pr.start_date_range, pr.state" +
                    " from exposition_storage.pull_request pr" +
                    " where to_date(pr.start_date_range, 'DD/MM/YYYY') " +
                    " >= to_date('01/02/2022','DD/MM/YYYY') " +
                    " and pr.organization_id = :organizationId " +
                    " and pr.vcs_repository_id in (" +
                    "    select ttr.repository_id from exposition_storage.team_to_repository ttr" +
                    "    where ttr.team_id = :teamId )")
    List<PullRequestTimeToMergeDTO> findTimeToMergeDTOsByOrganizationIdAndTeamId(@Param("organizationId") UUID organizationId,
                                                                                 @Param("teamId") UUID teamId);


    @Query(nativeQuery = true,
            value = " select pr.id, pr.days_opened, pr.start_date_range, pr.state" +
                    " from exposition_storage.pull_request pr" +
                    " where to_date(pr.start_date_range, 'DD/MM/YYYY') " +
                    " >= to_date('01/02/2022','DD/MM/YYYY')" +
                    " and pr.organization_id = :organizationId")
    List<PullRequestTimeToMergeDTO> findTimeToMergeDTOsByOrganizationId(@Param("organizationId") UUID organizationId);


}
