package fr.catlean.monolithic.backend.domain.domain.insight;

import fr.catlean.monolithic.backend.domain.model.insight.curve.PieceCurveWithAverage;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static fr.catlean.monolithic.backend.domain.query.PieceCurveWithAverageQueryTest.buildPullRequestTimeToMergeView;
import static org.assertj.core.api.Assertions.assertThat;

public class CurveTest {

    @Test
    void should_build_average_piece_curve_for_time_to_merge() {
        // Given
        final String startDateRange1 = "startDateRange1";
        final String startDateRange2 = "startDateRange2";
        final String startDateRange3 = "startDateRange3";

        // When
        final PieceCurveWithAverage pieceCurveWithAverage = PieceCurveWithAverage.buildTimeToMergeCurve(
                List.of(
                        buildPullRequestTimeToMergeView(3, startDateRange1, PullRequest.MERGE),
                        buildPullRequestTimeToMergeView(1, startDateRange1, PullRequest.CLOSE),
                        buildPullRequestTimeToMergeView(3, startDateRange2, PullRequest.MERGE),
                        buildPullRequestTimeToMergeView(3, startDateRange3, PullRequest.CLOSE),
                        buildPullRequestTimeToMergeView(3, startDateRange3, PullRequest.OPEN),
                        buildPullRequestTimeToMergeView(3, startDateRange3, PullRequest.OPEN)
                )
        );

        // Then
        assertThat(pieceCurveWithAverage.getAverageCurve().getData()).hasSize(3);
        assertThat(pieceCurveWithAverage.getPieceCurve().getData()).hasSize(6);
        assertThat(pieceCurveWithAverage.getPieceCurve().getData().get(0).getDate()).isEqualTo(startDateRange1);
        assertThat(pieceCurveWithAverage.getPieceCurve().getData().get(0).getValue()).isEqualTo(3);
        assertThat(pieceCurveWithAverage.getPieceCurve().getData().get(1).getDate()).isEqualTo(startDateRange1);
        assertThat(pieceCurveWithAverage.getPieceCurve().getData().get(1).getValue()).isEqualTo(1);
        assertThat(pieceCurveWithAverage.getPieceCurve().getData().get(2).getDate()).isEqualTo(startDateRange2);
        assertThat(pieceCurveWithAverage.getPieceCurve().getData().get(2).getValue()).isEqualTo(3);
        assertThat(pieceCurveWithAverage.getAverageCurve().getData().get(0).getDate()).isEqualTo(startDateRange3);
        assertThat(pieceCurveWithAverage.getAverageCurve().getData().get(1).getDate()).isEqualTo(startDateRange2);
        assertThat(pieceCurveWithAverage.getAverageCurve().getData().get(2).getDate()).isEqualTo(startDateRange1);
        assertThat(pieceCurveWithAverage.getAverageCurve().getData().get(0).getValue()).isEqualTo(3);
        assertThat(pieceCurveWithAverage.getAverageCurve().getData().get(1).getValue()).isEqualTo(3);
        assertThat(pieceCurveWithAverage.getAverageCurve().getData().get(2).getValue()).isEqualTo(2);

    }
}
