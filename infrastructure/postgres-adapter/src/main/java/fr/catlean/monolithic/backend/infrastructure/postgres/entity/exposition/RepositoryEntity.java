package fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition;

import com.sun.istack.NotNull;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.AbstractEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "repository", schema = "exposition_storage")
public class RepositoryEntity extends AbstractEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(generator = "exposition_storage.repository_sequence", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "exposition_storage.repository_sequence", schema = "exposition_storage", sequenceName =
            "repository_sequence", allocationSize = 1)
    Long id;
    @Column(name = "vcs_id", nullable = false)
    String vcsId;
    @Column(name = "name", nullable = false)
    @NotNull
    String name;
    @Column(name = "vcs_organization_name")
    String vcsOrganizationName;
    @Column(name = "organization_id")
    String organizationId;
}
