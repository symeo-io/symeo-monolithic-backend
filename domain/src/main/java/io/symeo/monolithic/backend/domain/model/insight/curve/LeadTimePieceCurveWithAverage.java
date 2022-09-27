package io.symeo.monolithic.backend.domain.model.insight.curve;


import io.symeo.monolithic.backend.domain.model.insight.CycleTime;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LeadTimePieceCurveWithAverage {
    @Builder.Default
    LeadTimePieceCurve leadTimePieceCurve = LeadTimePieceCurve.builder().build();
    @Builder.Default
    Curve averageCurve = Curve.builder().build();


    private void addPoint(final CycleTime cycleTime) {
        this.leadTimePieceCurve.addPoint(cycleTime.getPullRequestView().getStartDateRange(),
                cycleTime.getValue(), cycleTime.getCodingTime(), cycleTime.getReviewLag(), cycleTime.getReviewTime(),
                cycleTime.getPullRequestView().getVcsUrl(), cycleTime.getPullRequestView().getHead());
        this.averageCurve.addPoint(cycleTime.getPullRequestView().getStartDateRange(), cycleTime.getValue().floatValue());
    }

    public static LeadTimePieceCurveWithAverage buildPullRequestCurve(final List<CycleTime> cycleTimes) {
        final LeadTimePieceCurveWithAverage leadTimePieceCurveWithAverage =
                LeadTimePieceCurveWithAverage.builder().build();
        cycleTimes.forEach(leadTimePieceCurveWithAverage::addPoint);
        return leadTimePieceCurveWithAverage;
    }
}
