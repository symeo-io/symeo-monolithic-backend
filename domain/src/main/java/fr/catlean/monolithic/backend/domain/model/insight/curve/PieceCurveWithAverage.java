package fr.catlean.monolithic.backend.domain.model.insight.curve;

import fr.catlean.monolithic.backend.domain.model.insight.view.PullRequestTimeToMergeView;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
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

    private void addPoint(final PullRequestTimeToMergeView pullRequestTimeToMergeView) {
        this.pieceCurve.addPoint(pullRequestTimeToMergeView.getStartDateRange(),
                pullRequestTimeToMergeView.getDaysOpen(),
                pullRequestTimeToMergeView.getStatus().equals(PullRequest.OPEN));
        this.averageCurve.addPoint(pullRequestTimeToMergeView.getStartDateRange(),
                pullRequestTimeToMergeView.getDaysOpen());
    }


    public static PieceCurveWithAverage buildTimeToMergeCurve(final List<PullRequestTimeToMergeView> pullRequestTimeToMergeViews, final int limit) {
        final PieceCurveWithAverage pieceCurveWithAverage = PieceCurveWithAverage.builder().limit(limit).build();
        pullRequestTimeToMergeViews.forEach(pieceCurveWithAverage::addPoint);
        return pieceCurveWithAverage;
    }
}
