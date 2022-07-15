package fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition;

import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.PullRequestHistogramDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PullRequestHistogramRepository extends JpaRepository<PullRequestHistogramDataEntity, Long> {

    @Query(nativeQuery = true, value = "select * from exposition_storage.pull_request_histogram where " +
            "organization = " +
            ":organisationName and team = :teamName and histogram_type = :histogramType")
    List<PullRequestHistogramDataEntity> findByOrganizationNameAndTeamNameAndHistogramType(@Param("organisationName") String organizationName,
                                                                                           @Param("teamName") String teamName,
                                                                                           @Param("histogramType") String histogramType);
}
