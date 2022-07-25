package fr.catlean.monolithic.backend.infrastructure.postgres.repository.job;

import fr.catlean.monolithic.backend.infrastructure.postgres.entity.job.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRepository extends JpaRepository<JobEntity, Long>, JpaSpecificationExecutor<JobEntity> {

    @Query(
            nativeQuery = true,
            value = "select * from job_storage.job where code = :code and organization_id = :organizationId order by " +
                    "technical_modification_date desc"
    )
    List<JobEntity> findAllByCodeAndAndOrganizationIdOrderByTechnicalModificationDate(@Param("code") String code,
                                                                                      @Param("organizationId") String organizationId);
}
