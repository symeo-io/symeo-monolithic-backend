package fr.catlean.monolithic.backend.infrastructure.postgres.entity.account;

import fr.catlean.monolithic.backend.infrastructure.postgres.entity.AbstractEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "user", schema = "account")
public class UserEntity extends AbstractEntity {


    @Id
    @Column(name = "id", nullable = false)
    private String id;
    @Column(name = "mail", nullable = false)
    private String mail;
    @ManyToOne
    @JoinColumn(name = "organization_id",
            referencedColumnName = "id")
    private OrganizationEntity organizationEntity;

}
