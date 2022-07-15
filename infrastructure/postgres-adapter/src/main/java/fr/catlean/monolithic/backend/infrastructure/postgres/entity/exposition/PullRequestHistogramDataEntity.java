package fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition;

import com.sun.istack.NotNull;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.AbstractEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.id.HistogramId;
import lombok.*;
import org.hibernate.annotations.NaturalId;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "pull_request_histogram", schema = "exposition_storage")
public class PullRequestHistogramDataEntity extends AbstractEntity {

    @EmbeddedId
    @NaturalId
    HistogramId id;
    @Column(name = "data_below_limit")
    @NotNull
    int dataBelowLimit;
    @Column(name = "data_above_limit")
    @NotNull
    int dataAboveLimit;


}
