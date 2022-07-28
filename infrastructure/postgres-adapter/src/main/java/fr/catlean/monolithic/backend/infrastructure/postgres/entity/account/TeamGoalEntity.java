package fr.catlean.monolithic.backend.infrastructure.postgres.entity.account;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "team_goal", schema = "account_storage")
public class TeamGoalEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column(name = "standard_code", nullable = false)
    private String standardCode;
    @Column(name = "value", nullable = false)
    private String value;
    @Column(name = "team_id", nullable = false)
    private UUID teamId;
    @Column(name = "technical_creation_date", updatable = false)
    @CreationTimestamp
    ZonedDateTime technicalCreationDate;
    @UpdateTimestamp
    @Column(name = "technical_modification_date")
    ZonedDateTime technicalModificationDate;
}
