package io.symeo.monolithic.backend.domain.model.insight.curve;

import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PieceCurveWithAverage {
    @Builder.Default
    PieceCurve pieceCurve = PieceCurve.builder().build();
    @Builder.Default
    Curve averageCurve = Curve.builder().build();
    int limit;

    private void addPoint(final PullRequestView pullRequestView) {
        this.pieceCurve.addPoint(pullRequestView.getStartDateRange(),
                pullRequestView.getLimit(),
                pullRequestView.getStatus().equals(PullRequest.OPEN),
                pullRequestView.getBranchName(),
                pullRequestView.getVcsUrl());
        this.averageCurve.addPoint(pullRequestView.getStartDateRange(),
                pullRequestView.getLimit());
    }

    public static PieceCurveWithAverage buildPullRequestCurve(final List<PullRequestView> pullRequestLimitViews,
                                                              final int limit) {
        final PieceCurveWithAverage pieceCurveWithAverage = PieceCurveWithAverage.builder().limit(limit).build();
        pullRequestLimitViews.forEach(pieceCurveWithAverage::addPoint);
        return pieceCurveWithAverage;
    }


}
