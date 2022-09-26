package io.symeo.monolithic.backend.domain.model.insight.curve;


import io.symeo.monolithic.backend.domain.model.insight.LeadTime;
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


    private void addPoint(final LeadTime leadTime) {
        this.leadTimePieceCurve.addPoint(leadTime.getPullRequestView().getStartDateRange(),
                leadTime.getValue(), leadTime.getCodingTime(), leadTime.getReviewLag(), leadTime.getReviewTime(),
                leadTime.getPullRequestView().getVcsUrl(), leadTime.getPullRequestView().getHead());
        this.averageCurve.addPoint(leadTime.getPullRequestView().getStartDateRange(), leadTime.getValue().floatValue());
    }

    public static LeadTimePieceCurveWithAverage buildPullRequestCurve(final List<LeadTime> LeadTimes) {
        final LeadTimePieceCurveWithAverage leadTimePieceCurveWithAverage =
                LeadTimePieceCurveWithAverage.builder().build();
        LeadTimes.forEach(leadTimePieceCurveWithAverage::addPoint);
        return leadTimePieceCurveWithAverage;
    }
}
