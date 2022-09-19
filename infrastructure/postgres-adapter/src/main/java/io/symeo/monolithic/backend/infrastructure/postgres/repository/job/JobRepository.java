package io.symeo.monolithic.backend.infrastructure.postgres.repository.job;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.job.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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
            value = "select * from job_storage.job where code = :code and organization_id = :organizationId and " +
                    "team_id = :teamId" +
                    " order by technical_creation_date desc" +
                    " limit :numberOfJobsToFind"
    )
    List<JobEntity> findLastJobsForCodeAndOrganizationAndLimitAndTeamByTechnicalModificationDate(@Param("code") String code,
                                                                                                 @Param("organizationId") UUID organizationId,
                                                                                                 @Param("teamId") UUID teamId,
                                                                                                 @Param("numberOfJobsToFind") int numberOfJobsToFind);


    @Query(value = "update JobEntity set tasks = ( :tasks ) where id = :jobId")
    @Modifying
    @Transactional
    void updateTasksForJobId(@Param("jobId") Long jobId, @Param("tasks") String tasks);
}
