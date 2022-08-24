package io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition;

import com.sun.istack.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;

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
    @NotNull
    String id;
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
    @Column(name = "close_date")
    ZonedDateTime closeDate;
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
    @Column(name = "vcs_repository")
    String vcsRepository;
    @Column(name = "vcs_repository_id")
    String vcsRepositoryId;
    @Column(name = "vcs_organization_id")
    String vcsOrganizationId;
    @Column(name = "branch_name")
    String branchName;
    @Column(name = "organization_id")
    UUID organizationId;
    @Column(name = "size")
    float size;
    @Column(name = "days_opened")
    float daysOpened;
    @Column(name = "technical_creation_date", updatable = false)
    @CreationTimestamp
    ZonedDateTime technicalCreationDate;
    @UpdateTimestamp
    @Column(name = "technical_modification_date")
    ZonedDateTime technicalModificationDate;
}
