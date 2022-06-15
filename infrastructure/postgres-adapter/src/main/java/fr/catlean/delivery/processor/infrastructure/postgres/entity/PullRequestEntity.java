package fr.catlean.delivery.processor.infrastructure.postgres.entity;

import com.sun.istack.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NaturalId;
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
@Table(name = "pull_request", schema = "exposition_storage")
@EntityListeners(AuditingEntityListener.class)
public class PullRequestEntity {

    @Id
    @Column(name = "id", nullable = false)
    @NaturalId
    String id;
    @Column(name = "vcs_id", nullable = false)
    @NotNull
    int vcsId;
    @Column(name = "commit_number")
    int commitNumber;
    @Column(name = "deleted_line_number")
    int deletedLineNumber;
    @Column(name = "added_line_number")
    int addedLineNumber;
    @Column(name = "size")
    int size;
    @Column(name = "days_opened")
    long daysOpened;
    @Column(name = "start_date_range")
    String startDateRange;
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
    @Column(name = "team")
    String team;
    @Column(name = "vcs_repository")
    String vcsRepository;
    @Column(name = "vcs_organization")
    String vcsOrganization;
    @Column(name = "organization")
    String organization;
    @Column(name = "technical_creation_date", updatable = false)
    @CreationTimestamp
    ZonedDateTime technicalCreationDate;
    @UpdateTimestamp
    @Column(name = "technical_modification_date")
    ZonedDateTime technicalModificationDate;

}
