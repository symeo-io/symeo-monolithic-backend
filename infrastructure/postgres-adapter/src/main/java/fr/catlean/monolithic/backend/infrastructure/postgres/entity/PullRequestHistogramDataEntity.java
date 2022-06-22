package fr.catlean.monolithic.backend.infrastructure.postgres.entity;

import com.sun.istack.NotNull;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.id.HistogramId;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "pull_request_histogram", schema = "exposition_storage")
@EntityListeners(AuditingEntityListener.class)
public class PullRequestHistogramDataEntity {

    @EmbeddedId
    @NaturalId
    HistogramId id;
    @Column(name = "data_below_limit")
    @NotNull
    int dataBelowLimit;
    @Column(name = "data_above_limit")
    @NotNull
    int dataAboveLimit;
    @Column(name = "technical_creation_date", updatable = false)
    @CreationTimestamp
    ZonedDateTime technicalCreationDate;
    @UpdateTimestamp
    @Column(name = "technical_modification_date")
    ZonedDateTime technicalModificationDate;

}
