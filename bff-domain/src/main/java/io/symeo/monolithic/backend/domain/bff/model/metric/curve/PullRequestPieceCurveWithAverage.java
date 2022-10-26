package io.symeo.monolithic.backend.domain.bff.model.metric.curve;

import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PullRequestPieceCurveWithAverage {
    @Builder.Default
    PullRequestPieceCurve pullRequestPieceCurve = PullRequestPieceCurve.builder().build();
    @Builder.Default
    Curve averageCurve = Curve.builder().build();
    int limit;

    private void addPoint(final PullRequestView pullRequestView) {
        this.pullRequestPieceCurve.addPoint(pullRequestView.getStartDateRange(),
                pullRequestView.getLimit(),
                pullRequestView.getStatus().equals(PullRequestView.OPEN),
                pullRequestView.getHead(),
                pullRequestView.getVcsUrl());
        this.averageCurve.addPoint(pullRequestView.getStartDateRange(),
                pullRequestView.getLimit());
    }

    public static PullRequestPieceCurveWithAverage buildPullRequestCurve(final List<PullRequestView> pullRequestLimitViews,
                                                                         final int limit) {
        final PullRequestPieceCurveWithAverage pullRequestPieceCurveWithAverage = PullRequestPieceCurveWithAverage.builder().limit(limit).build();
        pullRequestLimitViews.forEach(pullRequestPieceCurveWithAverage::addPoint);
        return pullRequestPieceCurveWithAverage;
    }


}
