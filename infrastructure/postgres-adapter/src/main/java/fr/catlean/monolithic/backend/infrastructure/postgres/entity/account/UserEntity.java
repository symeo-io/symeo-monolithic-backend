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
@Table(name = "user", schema = "account_storage")
public class UserEntity extends AbstractEntity {


    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column(name = "email", nullable = false)
    private String email;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_to_organization",
            schema = "account_storage",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "organization_id")
    )
    private List<OrganizationEntity> organizationEntities;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "onboarding_id", referencedColumnName = "id")
    private OnboardingEntity onboardingEntity;
    @Column(name = "status", nullable = false)
    private String status;

}
