package fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition;

import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
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
@Table(name = "vcs_organization", schema = "exposition_storage")
@EntityListeners(AuditingEntityListener.class)
public class VcsOrganizationEntity  {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(generator = "vcs_organization_sequence", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "vcs_organization_sequence", schema = "exposition_storage", sequenceName =
            "vcs_organization_sequence", allocationSize = 1)
    Long id;
    @Column(name = "vcs_id", nullable = false)
    String vcsId;
    @Column(name = "name", nullable = false)
    String name;
    @Column(name = "external_id")
    String externalId;
    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    OrganizationEntity organizationEntity;
    @Column(name = "technical_creation_date", updatable = false)
    @CreationTimestamp
    ZonedDateTime technicalCreationDate;
    @UpdateTimestamp
    @Column(name = "technical_modification_date")
    ZonedDateTime technicalModificationDate;

}
