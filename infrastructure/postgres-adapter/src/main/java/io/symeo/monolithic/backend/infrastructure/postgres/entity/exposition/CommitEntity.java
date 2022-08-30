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
@Table(name = "commit", schema = "exposition_storage")
@EntityListeners(AuditingEntityListener.class)
public class CommitEntity {


    @Id
    @Column(name = "sha", nullable = false)
    @NotNull
    String sha;
    @Column(name = "author_login")
    String authorLogin;
    @Column(name = "message")
    String message;
    @Column(name = "date", nullable = false)
    ZonedDateTime date;
    @ManyToOne
    @JoinColumn(name = "pull_request_id", nullable = false, updatable = false)
    PullRequestEntity pullRequest;
    @Column(name = "technical_creation_date", updatable = false)
    @CreationTimestamp
    ZonedDateTime technicalCreationDate;
    @UpdateTimestamp
    @Column(name = "technical_modification_date")
    ZonedDateTime technicalModificationDate;
}
