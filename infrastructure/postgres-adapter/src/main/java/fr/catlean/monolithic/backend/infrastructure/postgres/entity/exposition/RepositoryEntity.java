package fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition;

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
@Table(name = "repository", schema = "exposition_storage")
@EntityListeners(AuditingEntityListener.class)
public class RepositoryEntity {

    @Id
    @Column(name = "id", nullable = false)
    String id;
    @Column(name = "name", nullable = false)
    @NotNull
    String name;
    @Column(name = "vcs_organization_name")
    String vcsOrganizationName;
    @Column(name = "organization_id")
    UUID organizationId;
    @Column(name = "technical_creation_date", updatable = false)
    @CreationTimestamp
    ZonedDateTime technicalCreationDate;
    @UpdateTimestamp
    @Column(name = "technical_modification_date")
    ZonedDateTime technicalModificationDate;
}
