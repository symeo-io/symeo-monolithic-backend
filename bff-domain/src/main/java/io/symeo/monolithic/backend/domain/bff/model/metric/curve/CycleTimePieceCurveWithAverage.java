package io.symeo.monolithic.backend.domain.bff.model.metric.curve;


import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTimeView;
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


    private void addPoint(final CycleTimeView cycleTimeView) {
        this.cycleTimePieceCurve.addPoint(cycleTimeView.getStartDateRange(),
                cycleTimeView.getValue(), cycleTimeView.getCodingTime(), cycleTimeView.getReviewTime(), cycleTimeView.getTimeToDeploy(),
                cycleTimeView.getPullRequestView().getVcsUrl(), cycleTimeView.getPullRequestView().getHead());
        this.averageCurve.addPoint(cycleTimeView.getStartDateRange(),
                cycleTimeView.getValue().floatValue());
    }

    public static CycleTimePieceCurveWithAverage buildPullRequestCurve(final List<CycleTimeView> cycleTimeViews) {
        final CycleTimePieceCurveWithAverage cycleTimePieceCurveWithAverage =
                CycleTimePieceCurveWithAverage.builder().build();
        cycleTimeViews.forEach(cycleTimePieceCurveWithAverage::addPoint);
        return cycleTimePieceCurveWithAverage;
    }
}
