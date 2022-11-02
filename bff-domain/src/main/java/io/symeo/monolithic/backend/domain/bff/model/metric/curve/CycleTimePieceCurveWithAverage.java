package io.symeo.monolithic.backend.domain.bff.model.metric.curve;


import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTime;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CycleTimePieceCurveWithAverage {
    @Builder.Default
    CycleTimePieceCurve cycleTimePieceCurve = CycleTimePieceCurve.builder().build();
    @Builder.Default
    Curve averageCurve = Curve.builder().build();


    private void addPoint(final CycleTime cycleTime) {
        this.cycleTimePieceCurve.addPoint(cycleTime.getStartDateRange(),
                cycleTime.getValue(), cycleTime.getCodingTime(), cycleTime.getReviewTime(), cycleTime.getTimeToDeploy(),
                cycleTime.getPullRequestView().getVcsUrl(), cycleTime.getPullRequestView().getHead());
        this.averageCurve.addPoint(cycleTime.getPullRequestView().getStartDateRange(),
                cycleTime.getValue().floatValue());
    }

    public static CycleTimePieceCurveWithAverage buildPullRequestCurve(final List<CycleTime> cycleTimes) {
        final CycleTimePieceCurveWithAverage cycleTimePieceCurveWithAverage =
                CycleTimePieceCurveWithAverage.builder().build();
        cycleTimes.forEach(cycleTimePieceCurveWithAverage::addPoint);
        return cycleTimePieceCurveWithAverage;
    }
}
