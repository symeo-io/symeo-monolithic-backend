package fr.catlean.monolithic.backend.infrastructure.postgres.entity.job;

import fr.catlean.monolithic.backend.infrastructure.postgres.entity.AbstractEntity;
import lombok.*;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "job", schema = "job_storage")
public class JobEntity extends AbstractEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(generator = "job_sequence", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "job_sequence", schema = "job_storage", sequenceName =
            "job_sequence", allocationSize = 1)
    private Long id;
    @Column(name = "code", nullable = false)
    private String code;
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;
    @Column(name = "status", nullable = false)
    private String status;
    @Column(name = "end_date")
    private ZonedDateTime endDate;
}
