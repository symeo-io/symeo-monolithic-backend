package fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition;

import com.sun.istack.NotNull;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.AbstractEntity;
import lombok.*;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "pull_request", schema = "exposition_storage")
@NamedEntityGraph(name = "PullRequestEntity.TimeToMerge",
        attributeNodes = {
                @NamedAttributeNode("startDateRange"),
                @NamedAttributeNode("daysOpened"),
                @NamedAttributeNode("state")
        })
public class PullRequestEntity extends AbstractEntity {

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
    @Column(name = "size")
    int size;
    @Column(name = "days_opened")
    int daysOpened;
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
    @Column(name = "vcs_repository")
    String vcsRepository;
    @Column(name = "vcs_organization")
    String vcsOrganization;
    @Column(name = "organization_id")
    String organizationId;

}
