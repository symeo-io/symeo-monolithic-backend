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
    long value;
    @Column(name = "coding_time")
    long codingTime;
    @Column(name = "review_time")
    long reviewTime;
    @Column(name = "time_to_deploy")
    long timeToDeploy;
    @Column(name = "deploy_date")
    Date deployDate;
    @Column(name = "pull_request_id", nullable = false, updatable = false)
    String pullRequestId;
}
