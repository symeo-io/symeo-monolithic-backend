package fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition;

import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.dto.PullRequestTimeToMergeDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PullRequestTimeToMergeRepository extends JpaRepository<PullRequestTimeToMergeDTO, String> {

    @Query(nativeQuery = true,
            value = " select id, days_opened, start_date_range, state" +
                    " from exposition_storage.pull_request" +
                    " where to_date(exposition_storage.pull_request.start_date_range, 'DD/MM/YYYY') >= to_date" +
                    "('01/02/2022','DD/MM/YYYY')" +
                    " and organization_id = :organizationId")
    List<PullRequestTimeToMergeDTO> findTimeToMergeDTOsByOrganizationId(@Param("organizationId") UUID organizationId);
}
