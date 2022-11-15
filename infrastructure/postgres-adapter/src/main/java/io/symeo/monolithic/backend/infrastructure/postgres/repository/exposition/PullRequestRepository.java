package io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.PullRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface PullRequestRepository extends JpaRepository<PullRequestEntity, String>, JpaSpecificationExecutor<PullRequestEntity> {

    @Query(nativeQuery = true,
            value = "select * " +
                    " from exposition_storage.pull_request pr" +
                    " where pr.state = 'merge' " +
                    " and pr.merge_date <= :endDate " +
                    " and pr.vcs_repository_id = :repositoryId")
    List<PullRequestEntity> findAllMergedPullRequestsForRepositoryIdUntilEndDate(@Param("repositoryId") String repositoryId,
                                                                                 @Param("endDate") Date endDate);
}
