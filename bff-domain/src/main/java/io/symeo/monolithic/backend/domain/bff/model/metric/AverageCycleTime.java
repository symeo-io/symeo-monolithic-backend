package io.symeo.monolithic.backend.domain.bff.model.metric;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AverageCycleTime {
    Float averageValue;
    Float averageCodingTime;
    Float averageReviewTime;
    Float averageTimeToDeploy;
}
