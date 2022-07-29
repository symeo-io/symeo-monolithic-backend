package fr.catlean.monolithic.backend.infrastructure.postgres.entity.account;

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
@Table(name = "team", schema = "account_storage")
@EntityListeners(AuditingEntityListener.class)
public class TeamEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "organization_id", nullable = false, updatable = false)
    private UUID organizationId;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "team_to_repository", schema = "exposition_storage",
            joinColumns = @JoinColumn(name = "team_id")
    )
    @Column(name = "repository_id")
    @Builder.Default
    List<String> repositoryIds = new ArrayList<>();
    @Column(name = "technical_creation_date", updatable = false)
    @CreationTimestamp
    ZonedDateTime technicalCreationDate;
    @UpdateTimestamp
    @Column(name = "technical_modification_date")
    ZonedDateTime technicalModificationDate;

}
