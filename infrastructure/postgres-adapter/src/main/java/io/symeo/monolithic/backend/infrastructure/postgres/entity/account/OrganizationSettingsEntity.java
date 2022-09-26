package io.symeo.monolithic.backend.infrastructure.postgres.entity.account;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "organization_settings", schema = "organization_storage")
@EntityListeners(AuditingEntityListener.class)
public class OrganizationSettingsEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;
    @Column(name = "tag_regex")
    private String tagRegex;
    @Column(name = "pr_merged_on_branch_regex")
    private String pullRequestMergedOnBranchRegex;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "organization_settings_exclude_branch_regex", schema = "organization_storage",
            joinColumns = @JoinColumn(name = "organization_settings_id")
    )
    @Column(name = "exclude_branch_regex")
    @Builder.Default
    List<String> excludeBranchRegexes = new ArrayList<>();
    @Column(name = "technical_creation_date", updatable = false)
    @CreationTimestamp
    ZonedDateTime technicalCreationDate;
    @UpdateTimestamp
    @Column(name = "technical_modification_date")
    ZonedDateTime technicalModificationDate;
}
