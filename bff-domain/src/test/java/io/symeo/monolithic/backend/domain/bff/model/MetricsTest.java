package io.symeo.monolithic.backend.domain.bff.model;

import io.symeo.monolithic.backend.domain.bff.model.metric.Metrics;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
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
        final Date currentStartDate = stringToDate("2020-02-01");
        final Date previousStartDate = stringToDate("2020-01-01");

        // When
        final Metrics metrics = Metrics.buildTimeToMergeMetricsFromPullRequests(teamGoalLimit, currentEndDate,
                currentStartDate, previousStartDate, currentPRView,
                previousPRView);

        // Then
        assertThat(metrics).isNotNull();
        assertThat(metrics.getCurrentAverage()).isEqualTo(19.6);
        assertThat(metrics.getCurrentPercentage()).isEqualTo(40D);
        assertThat(metrics.getPreviousAverage()).isEqualTo(13.3);
        assertThat(metrics.getPreviousPercentage()).isEqualTo(25D);
        assertThat(metrics.getAverageTendency()).isEqualTo(47.4);
        assertThat(metrics.getPercentageTendency()).isEqualTo(60D);
        assertThat(metrics.getCurrentEndDate()).isEqualTo(currentEndDate);
        assertThat(metrics.getCurrentStartDate()).isEqualTo(currentStartDate);
        assertThat(metrics.getPreviousEndDate()).isEqualTo(currentStartDate);
        assertThat(metrics.getPreviousStartDate()).isEqualTo(previousStartDate);
    }


    @Test
    void should_compute_pull_request_size_metrics_given_pull_requests_and_team_goal_limit() throws SymeoException {
        // Given
        final List<PullRequestView> previousPRViews = List.of(
                PullRequestView.builder().addedLineNumber(200).deletedLineNumber(100).build(),
                PullRequestView.builder().addedLineNumber(500).deletedLineNumber(500).build(),
                PullRequestView.builder().addedLineNumber(1000).deletedLineNumber(500).build(),
                PullRequestView.builder().addedLineNumber(200).deletedLineNumber(2000).build(),
                PullRequestView.builder().addedLineNumber(2000).deletedLineNumber(2000).build()
        );
        final List<PullRequestView> currentPRViews = List.of(
                PullRequestView.builder().addedLineNumber(200).deletedLineNumber(100).build(),
                PullRequestView.builder().addedLineNumber(500).deletedLineNumber(500).build(),
                PullRequestView.builder().addedLineNumber(100).deletedLineNumber(500).build(),
                PullRequestView.builder().addedLineNumber(200).deletedLineNumber(2000).build(),
                PullRequestView.builder().addedLineNumber(200).deletedLineNumber(300).build(),
                PullRequestView.builder().addedLineNumber(200).deletedLineNumber(100).build(),
                PullRequestView.builder().addedLineNumber(200).deletedLineNumber(20).build()
        );
        final int limit = 1000;
        final Date currentEndDate = stringToDate("2020-01-01");
        final Date currentStartDate = stringToDate("2020-02-01");
        final Date previousStartDate = stringToDate("2020-03-01");

        // When
        final Metrics metrics = Metrics.buildSizeMetricsFromPullRequests(limit, currentEndDate,
                currentStartDate, previousStartDate, currentPRViews, previousPRViews);

        // Then
        assertThat(metrics).isNotNull();
        assertThat(metrics.getCurrentAverage()).isEqualTo(731.4);
        assertThat(metrics.getCurrentPercentage()).isEqualTo(85.7);
        assertThat(metrics.getPreviousAverage()).isEqualTo(1800.0);
        assertThat(metrics.getPreviousPercentage()).isEqualTo(40);
        assertThat(metrics.getAverageTendency()).isEqualTo(-59.4);
        assertThat(metrics.getPercentageTendency()).isEqualTo(114.3);
        assertThat(metrics.getCurrentEndDate()).isEqualTo(currentEndDate);
        assertThat(metrics.getCurrentStartDate()).isEqualTo(currentStartDate);
        assertThat(metrics.getPreviousEndDate()).isEqualTo(currentStartDate);
        assertThat(metrics.getPreviousStartDate()).isEqualTo(previousStartDate);
    }

    @Test
    void should_return_zero_metrics_for_empty_pull_requests() {
        // Given

        // When
        final Metrics timeToMergeMetricsFromPullRequests = Metrics.buildTimeToMergeMetricsFromPullRequests(5,
                new Date(), new Date(), new Date(), List.of(),
                List.of());
        final Metrics sizeMetricsFromPullRequests = Metrics.buildSizeMetricsFromPullRequests(1000, new Date(),
                new Date(), new Date(), List.of(),
                List.of());

        // Then
        assertThat(timeToMergeMetricsFromPullRequests.getPercentageTendency()).isEqualTo(0D);
        assertThat(timeToMergeMetricsFromPullRequests.getAverageTendency()).isEqualTo(0D);
        assertThat(timeToMergeMetricsFromPullRequests.getCurrentAverage()).isEqualTo(0D);
        assertThat(timeToMergeMetricsFromPullRequests.getPreviousAverage()).isEqualTo(0D);
        assertThat(timeToMergeMetricsFromPullRequests.getCurrentPercentage()).isEqualTo(0D);
        assertThat(timeToMergeMetricsFromPullRequests.getPreviousPercentage()).isEqualTo(0D);
        assertThat(sizeMetricsFromPullRequests.getPercentageTendency()).isEqualTo(0D);
        assertThat(sizeMetricsFromPullRequests.getAverageTendency()).isEqualTo(0D);
        assertThat(sizeMetricsFromPullRequests.getCurrentAverage()).isEqualTo(0D);
        assertThat(sizeMetricsFromPullRequests.getPreviousAverage()).isEqualTo(0D);
        assertThat(sizeMetricsFromPullRequests.getCurrentPercentage()).isEqualTo(0D);
        assertThat(sizeMetricsFromPullRequests.getPreviousPercentage()).isEqualTo(0D);

    }
}
