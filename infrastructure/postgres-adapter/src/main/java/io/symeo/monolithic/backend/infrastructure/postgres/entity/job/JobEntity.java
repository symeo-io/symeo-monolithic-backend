package io.symeo.monolithic.backend.infrastructure.postgres.entity.job;

import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
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
@Table(name = "job", schema = "job_storage")
@EntityListeners(AuditingEntityListener.class)
@TypeDef(
        name = "json",
        typeClass = JsonType.class
)
public class JobEntity {

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
    @Column(name = "error")
    String error;
    @Lob
    @Type(type = "json")
    @Column(name = "tasks", nullable = false, columnDefinition = "jsonb")
    String tasks;
    @Column(name = "end_date")
    private ZonedDateTime endDate;
    @Column(name = "technical_creation_date", updatable = false)
    @CreationTimestamp
    ZonedDateTime technicalCreationDate;
    @UpdateTimestamp
    @Column(name = "technical_modification_date")
    ZonedDateTime technicalModificationDate;
}
