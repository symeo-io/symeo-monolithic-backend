package fr.catlean.monolithic.backend.infrastructure.postgres.entity.account;

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
@Table(name = "organization", schema = "account")
public class OrganizationEntity extends AbstractEntity {

    @Id
    @Column(name = "id", nullable = false)
    @NaturalId
    String id;
    @Column(name = "name", nullable = false)
    String name;
    @Column(name = "external_id", nullable = false)
    String externalId;
}
