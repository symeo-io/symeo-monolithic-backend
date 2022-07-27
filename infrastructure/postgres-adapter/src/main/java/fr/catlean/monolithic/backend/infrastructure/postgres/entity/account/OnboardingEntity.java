package fr.catlean.monolithic.backend.infrastructure.postgres.entity.account;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "onboarding", schema = "account_storage")
@EntityListeners(AuditingEntityListener.class)
public class OnboardingEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column(name = "has_connected_to_vcs", nullable = false)
    private Boolean hasConnectedToVcs;
    @Column(name = "has_configured_team", nullable = false)
    private Boolean hasConfiguredTeam;
    @Column(name = "technical_creation_date", updatable = false)
    @CreationTimestamp
    ZonedDateTime technicalCreationDate;
    @UpdateTimestamp
    @Column(name = "technical_modification_date")
    ZonedDateTime technicalModificationDate;
}
