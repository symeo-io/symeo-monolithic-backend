package fr.catlean.monolithic.backend.infrastructure.postgres.entity.account;

import fr.catlean.monolithic.backend.infrastructure.postgres.entity.AbstractEntity;
import lombok.*;

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
@Table(name = "onboarding", schema = "account_storage")
public class OnboardingEntity extends AbstractEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String id;
    @Column(name = "has_connected_to_vcs", nullable = false)
    private Boolean hasConnectedToVcs;
    @Column(name = "has_configured_team", nullable = false)
    private Boolean hasConfiguredTeam;
}
