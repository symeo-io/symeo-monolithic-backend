package io.symeo.monolithic.backend.domain.model.insight;

import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import lombok.Builder;
import lombok.Value;

import java.util.Date;
import java.util.List;

@Value
@Builder(toBuilder = true)
public class Metrics {

    Double currentAverage;
    Double previousAverage;
    Double currentPercentage;
    Double previousPercentage;

    public static Metrics buildTimeToMergeMetricsFromPullRequests(final int limit, final Date currentEndDate,
                                                                  final Date previousEndDate,
                                                                  final List<PullRequestView> currentPullRequestViews,
                                                                  final List<PullRequestView> previousPullRequestViews) {
        final Metrics currentMetrics = computeAverageAndPercentageTimeToMergeMetric(limit, currentEndDate,
                currentPullRequestViews);
        final Metrics previousMetrics = computeAverageAndPercentageTimeToMergeMetric(limit, previousEndDate,
                previousPullRequestViews);

        return currentMetrics.toBuilder()
                .previousAverage(previousMetrics.currentAverage)
                .previousPercentage(previousMetrics.currentPercentage)
                .build();
    }

    public static Metrics buildSizeMetricsFromPullRequests(final int limit,
                                                           final List<PullRequestView> currentPullRequestViews,
                                                           final List<PullRequestView> previousPullRequestViews) {
        final Metrics currentMetrics = computeAverageAndPercentagePullRequestSizeMetric(limit,
                currentPullRequestViews);
        final Metrics previousMetrics = computeAverageAndPercentagePullRequestSizeMetric(limit,
                previousPullRequestViews);

        return currentMetrics.toBuilder()
                .previousAverage(previousMetrics.currentAverage)
                .previousPercentage(previousMetrics.currentPercentage)
                .build();
    }

    private static Metrics computeAverageAndPercentagePullRequestSizeMetric(int limit,
                                                                            List<PullRequestView> currentPullRequestViews) {
        double average = 0D;
        double percentage = 0D;
        int number = 0;
        for (PullRequestView currentPullRequestView : currentPullRequestViews) {
            average += currentPullRequestView.getSize();
            percentage += currentPullRequestView.getSize() <= limit ? 1 : 0;
            number += 1;
        }
        return Metrics.builder()
                .currentAverage(Math.round(10 * average / number) / 10D)
                .currentPercentage(Math.round(10 * 100 * percentage / number) / 10D)
                .build();
    }

    private static Metrics computeAverageAndPercentageTimeToMergeMetric(final int limit, final Date currentEndDate,
                                                                        final List<PullRequestView> currentPullRequestViews) {
        double average = 0D;
        double percentage = 0D;
        int number = 0;
        for (PullRequestView currentPullRequestView : currentPullRequestViews) {
            average += currentPullRequestView.getDaysOpened(currentEndDate);
            percentage += currentPullRequestView.getDaysOpened(currentEndDate) <= limit ? 1 : 0;
            number += 1;
        }
        return Metrics.builder()
                .currentAverage(Math.round(10 * average / number) / 10D)
                .currentPercentage(Math.round(10 * 100 * percentage / number) / 10D)
                .build();
    }

    public Double getAverageTendency() {
        return Math.round(10 * computeTendency(this.currentAverage, this.previousAverage)) / 10D;
    }

    public Double getPercentageTendency() {
        return Math.round(10 * computeTendency(this.currentPercentage, this.previousPercentage)) / 10D;
    }

    private static Double computeTendency(final Double currentValue, final Double previousValue) {
        return previousValue == 0 ? 0 : 100 * (currentValue - previousValue) / previousValue;
    }
}
