package fr.catlean.delivery.processor.infrastructure.postgres.entity;

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
@Table(name = "pull_request")
@EntityListeners(AuditingEntityListener.class)
public class PullRequestEntity {

    @Id
    @Column(name = "pk")
    @GeneratedValue(generator = "pull_request_sequence", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "pull_request_sequence", sequenceName = "pull_request_sequence", allocationSize = 1)
    Long pk;

    @Column(name = "number", nullable = false)
    @NotNull
    int number;
    @Column(name = "commit_number")
    int commitNumber;
    @Column(name = "deleted_line_number")
    int deletedLineNumber;
    @Column(name = "added_line_number")
    int addedLineNumber;
    @Column(name = "creation_date", nullable = false)
    ZonedDateTime creationDate;
    @Column(name = "last_update_date", nullable = false)
    ZonedDateTime lastUpdateDate;
    @Column(name = "merge_date")
    ZonedDateTime mergeDate;
    @Column(name = "is_merged")
    Boolean isMerged;
    @Column(name = "is_draft")
    Boolean isDraft;
    @Column(name = "state")
    String state;
    @Column(name = "vcs_url")
    String vcsUrl;
    @Column(name = "title")
    String title;
    @Column(name = "author_login", nullable = false)
    String authorLogin;
    @Column(name = "technical_creation_date", updatable = false)
    @CreationTimestamp
    ZonedDateTime technicalCreationDate;
    @UpdateTimestamp
    @Column(name = "technical_modification_date")
    ZonedDateTime technicalModificationDate;

}
