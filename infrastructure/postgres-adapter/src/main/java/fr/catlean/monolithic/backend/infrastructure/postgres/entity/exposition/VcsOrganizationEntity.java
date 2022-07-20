package fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition;

import fr.catlean.monolithic.backend.infrastructure.postgres.entity.AbstractEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "vcs_organization", schema = "exposition_storage")
public class VcsOrganizationEntity extends AbstractEntity {

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
    @OneToOne
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    OrganizationEntity organizationEntity;


}
