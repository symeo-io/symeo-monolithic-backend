package io.symeo.monolithic.backend.infrastructure.postgres.repository.job;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.job.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JobRepository extends JpaRepository<JobEntity, Long>, JpaSpecificationExecutor<JobEntity> {

    @Query(
            nativeQuery = true,
            value = "select * from job_storage.job where code = :code and organization_id = :organizationId" +
                    " order by technical_modification_date desc"
    )
    List<JobEntity> findAllByCodeAndAndOrganizationIdOrderByTechnicalModificationDate(@Param("code") String code,
                                                                                      @Param("organizationId") UUID organizationId);


    @Query(
            nativeQuery = true,
            value = "select * from job_storage.job where code = :code and organization_id = :organizationId" +
                    " order by technical_creation_date desc" +
                    " limit :numberOfJobsToFind"
    )
    List<JobEntity> findLastJobsForCodeAndOrganizationAndLimitByTechnicalModificationDate(@Param("code") String code,
                                                                                           @Param("organizationId") UUID organizationId,
                                                                                           @Param("numberOfJobsToFind") int numberOfJobsToFind);
}
