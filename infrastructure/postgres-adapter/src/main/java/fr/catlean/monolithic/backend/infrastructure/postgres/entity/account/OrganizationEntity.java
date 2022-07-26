package fr.catlean.monolithic.backend.infrastructure.postgres.entity.account;

import fr.catlean.monolithic.backend.infrastructure.postgres.entity.AbstractEntity;
import lombok.*;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "organization", schema = "account_storage")
public class OrganizationEntity extends AbstractEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column(name = "name", nullable = false)
    private String name;
    @ManyToMany(mappedBy = "organizationEntities")
    List<UserEntity> userEntities;
}
