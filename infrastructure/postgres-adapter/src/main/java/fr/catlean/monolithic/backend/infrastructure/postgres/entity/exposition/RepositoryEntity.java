package fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition;

import com.sun.istack.NotNull;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.AbstractEntity;
import lombok.*;
import org.hibernate.annotations.NaturalId;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.ZonedDateTime;

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
    @Column(name = "vcs_id", nullable = false)
    @NotNull
    String vcsId;
    @Column(name = "vcs_organization_name")
    String vcsOrganizationName;
    @Column(name = "organization_id")
    String organizationId;
    @Column(name = "creation_date", nullable = false)
    ZonedDateTime creationDate;
    @Column(name = "last_update_date", nullable = false)
    ZonedDateTime lastUpdateDate;
}
