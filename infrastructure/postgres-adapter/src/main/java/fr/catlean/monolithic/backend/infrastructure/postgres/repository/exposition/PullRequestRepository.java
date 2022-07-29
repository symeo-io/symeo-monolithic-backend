package fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition;

import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.PullRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PullRequestRepository extends JpaRepository<PullRequestEntity, String>,
        JpaSpecificationExecutor<PullRequestEntity> {

    List<PullRequestEntity> findAllByOrganizationId(UUID organizationId);

    @Query(nativeQuery = true,
            value = " select pr.*" +
                    " from exposition_storage.pull_request pr" +
                    " where to_date(pr.start_date_range, 'DD/MM/YYYY') " +
                    " >= to_date('01/02/2022','DD/MM/YYYY') " +
                    " and pr.organization_id = :organizationId " +
                    " and pr.vcs_repository_id in (" +
                    "    select ttr.repository_id from exposition_storage.team_to_repository ttr" +
                    "    where ttr.team_id = :teamId )")
    List<PullRequestEntity> findAllByOrganizationIdAndTeamId(@Param("organizationId") UUID organizationId,
                                                             @Param("teamId") UUID teamId);

}
