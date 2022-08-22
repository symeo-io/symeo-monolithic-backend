package io.symeo.monolithic.backend.domain.domain;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.insight.Metrics;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.assertj.core.api.Assertions.assertThat;

public class MetricsTest {


    @Test
    void should_compute_time_to_merge_metrics_given_pull_requests_and_team_goal_limit() throws SymeoException {
        // Given
        final List<PullRequestView> previousPRView = List.of(
                PullRequestView.builder()
                        .creationDate(stringToDate("2020-01-01"))
                        .closeDate(null)
                        .mergeDate(null)
                        .build(),
                PullRequestView.builder()
                        .creationDate(stringToDate("2020-01-01"))
                        .closeDate(null)
                        .mergeDate(stringToDate("2020-01-12"))
                        .build(),
                PullRequestView.builder()
                        .creationDate(stringToDate("2020-01-01"))
                        .closeDate(null)
                        .mergeDate(stringToDate("2020-01-10"))
                        .build(),
                PullRequestView.builder()
                        .creationDate(stringToDate("2020-01-01"))
                        .closeDate(null)
                        .mergeDate(stringToDate("2020-01-03"))
                        .build()
        );
        final List<PullRequestView> currentPRView = List.of(
                PullRequestView.builder()
                        .creationDate(stringToDate("2020-02-01"))
                        .closeDate(null)
                        .mergeDate(stringToDate("2020-02-01"))
                        .build(),
                PullRequestView.builder()
                        .creationDate(stringToDate("2020-01-01"))
                        .closeDate(null)
                        .mergeDate(stringToDate("2020-02-02"))
                        .build(),
                PullRequestView.builder()
                        .creationDate(stringToDate("2020-02-01"))
                        .closeDate(null)
                        .mergeDate(stringToDate("2020-02-03"))
                        .build(),
                PullRequestView.builder()
                        .creationDate(stringToDate("2020-01-01"))
                        .closeDate(null)
                        .mergeDate(stringToDate("2020-02-05"))
                        .build(),
                PullRequestView.builder()
                        .creationDate(stringToDate("2020-02-01"))
                        .closeDate(null)
                        .mergeDate(null)
                        .build()
        );
        final int teamGoalLimit = 5;
        final Date currentEndDate = stringToDate("2020-03-01");
        final Date previousEndDate = stringToDate("2020-02-01");

        // When
        final Metrics metrics = Metrics.buildTimeToMergeMetricsFromPullRequests(teamGoalLimit, currentEndDate,
                previousEndDate, currentPRView,
                previousPRView);

        // Then
        assertThat(metrics).isNotNull();
        assertThat(metrics.getCurrentAverage()).isEqualTo(19.6);
        assertThat(metrics.getCurrentPercentage()).isEqualTo(40D);
        assertThat(metrics.getPreviousAverage()).isEqualTo(13.3);
        assertThat(metrics.getPreviousPercentage()).isEqualTo(25D);
        assertThat(metrics.getAverageTendency()).isEqualTo(47.4);
        assertThat(metrics.getPercentageTendency()).isEqualTo(60D);
    }

}
