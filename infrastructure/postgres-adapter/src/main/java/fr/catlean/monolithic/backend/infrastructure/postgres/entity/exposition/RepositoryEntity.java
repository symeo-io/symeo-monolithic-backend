package fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition;

import com.sun.istack.NotNull;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.AbstractEntity;
import lombok.*;
import org.hibernate.annotations.NaturalId;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
    @NaturalId
    String id;
    @Column(name = "name", nullable = false)
    @NotNull
    String name;
    @Column(name = "vcs_organization_name")
    String vcsOrganizationName;
    @Column(name = "organization_id")
    String organizationId;
}
