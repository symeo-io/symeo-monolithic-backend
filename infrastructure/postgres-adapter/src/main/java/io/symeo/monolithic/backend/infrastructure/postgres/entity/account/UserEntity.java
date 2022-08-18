package io.symeo.monolithic.backend.infrastructure.postgres.entity.account;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "user", schema = "account_storage")
@EntityListeners(AuditingEntityListener.class)
public class UserEntity  {

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
    @Column(name = "technical_creation_date", updatable = false)
    @CreationTimestamp
    ZonedDateTime technicalCreationDate;
    @UpdateTimestamp
    @Column(name = "technical_modification_date")
    ZonedDateTime technicalModificationDate;

}
