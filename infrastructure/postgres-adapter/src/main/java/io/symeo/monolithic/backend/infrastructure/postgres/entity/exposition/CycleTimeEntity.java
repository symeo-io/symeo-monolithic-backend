package io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "cycle_time", schema = "exposition_storage")
@EntityListeners(AuditingEntityListener.class)
public class CycleTimeEntity {
    @Id
    @Column(name = "id", nullable = false)
    String id;
    @Column(name = "value")
    Long value;
    @Column(name = "coding_time")
    Long codingTime;
    @Column(name = "review_time")
    Long reviewTime;
    @Column(name = "time_to_deploy")
    Long timeToDeploy;
    @Column(name = "deploy_date")
    Date deployDate;
    @Column(name = "pull_request_id", nullable = false, updatable = false)
    String pullRequestId;
    @Column(name = "pull_request_author_login", nullable = false)
    String pullRequestAuthorLogin;
    @Column(name = "pull_request_state", nullable = false)
    String pullRequestState;
    @Column(name = "pull_request_vcs_repository_id", nullable = false, updatable = false)
    String pullRequestVcsRepositoryId;
    @Column(name = "pull_request_vcs_repository")
    String pullRequestVcsRepository;
    @Column(name = "pull_request_vcs_url")
    String pullRequestVcsUrl;
    @Column(name = "pull_request_title")
    String pullRequestTitle;
    @Column(name = "pull_request_creation_date")
    Date pullRequestCreationDate;
    @Column(name = "pull_request_update_date")
    Date pullRequestUpdateDate;
    @Column(name = "pull_request_merge_date")
    Date pullRequestMergeDate;
    @Column(name = "pull_request_head")
    String pullRequestHead;
}
