package fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.id;

import com.sun.istack.NotNull;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@Data
public class HistogramId implements Serializable {

    @Column(name = "start_date_range", nullable = false)
    @NotNull
    String startDateRange;
    @Column(name = "organization_id", nullable = false)
    @NotNull
    String organizationId;
    @Column(name = "team", nullable = false)
    @NotNull
    String teamName;
    @NotNull
    @Column(name = "histogram_type", nullable = false)
    String histogramType;

}
