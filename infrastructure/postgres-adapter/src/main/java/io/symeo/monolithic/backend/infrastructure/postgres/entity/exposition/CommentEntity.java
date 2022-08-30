package io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition;

import com.sun.istack.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "comment", schema = "exposition_storage")
@EntityListeners(AuditingEntityListener.class)
public class CommentEntity {
    @Id
    @Column(name = "id", nullable = false)
    @NotNull
    String id;
    @Column(name = "pull_request_id", nullable = false)
    String pullRequestId;
    @Column(name = "creation_date", nullable = false)
    ZonedDateTime creationDate;
    @ManyToOne
    @JoinColumn(name = "pull_request_id", nullable = false, updatable = false, insertable = false)
    PullRequestEntity pullRequest;
    @Column(name = "technical_creation_date", updatable = false)
    @CreationTimestamp
    ZonedDateTime technicalCreationDate;
    @UpdateTimestamp
    @Column(name = "technical_modification_date")
    ZonedDateTime technicalModificationDate;
}
