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
@Table(name = "user", schema = "account_storage")
public class UserEntity extends AbstractEntity {


    @Id
    @Column(name = "id", nullable = false)
    private String id;
    @Column(name = "email", nullable = false)
    private String email;
    @ManyToOne
    @JoinColumn(name = "organization_id",
            referencedColumnName = "id")
    private OrganizationEntity organizationEntity;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "onboarding_id", referencedColumnName = "id")
    private OnboardingEntity onboardingEntity;
    @Column(name = "organization_id", insertable = false,updatable = false)
    private String organizationId;
    @Column(name = "status",nullable = false)
    private String status;

}
