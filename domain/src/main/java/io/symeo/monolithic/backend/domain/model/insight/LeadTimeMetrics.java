package io.symeo.monolithic.backend.domain.model.insight;

import lombok.Builder;
import lombok.Value;

import java.util.Optional;

import static java.lang.Math.round;
import static java.util.Optional.empty;
import static java.util.Optional.of;

@Value
@Builder
public class LeadTimeMetrics {
    private static final Float EMPTY_PERCENTAGE = 0.0f;
    Float average;
    Float averageTendencyPercentage;
    Float averageCodingTime;
    Float averageCodingTimePercentageTendency;
    Float averageReviewLag;
    Float averageReviewLagPercentageTendency;
    Float averageReviewTime;
    Float averageReviewTimePercentageTendency;


    public static Optional<LeadTimeMetrics> buildFromCurrentAndPreviousLeadTimes(final Optional<LeadTime> optionalCurrentLeadTime,
                                                                                 final Optional<LeadTime> optionalPreviousLeadTime) {
        if (optionalPreviousLeadTime.isEmpty() && optionalCurrentLeadTime.isEmpty()) {
            return empty();
        } else if (optionalCurrentLeadTime.isPresent() && optionalPreviousLeadTime.isPresent()) {
            final LeadTime currentLeadTime = optionalCurrentLeadTime.get();
            final LeadTime previousLeadTime = optionalPreviousLeadTime.get();
            return getLeadTimeMetricsForCurrentAndPreviousLeadTimePresents(currentLeadTime, previousLeadTime);
        } else if (optionalCurrentLeadTime.isPresent()) {
            final LeadTime currentLeadTime = optionalCurrentLeadTime.get();
            return getLeadTimeMetricsWithNoPreviousLeadTime(currentLeadTime);
        } else {
            return empty();
        }
    }

    private static Optional<LeadTimeMetrics> getLeadTimeMetricsWithNoPreviousLeadTime(LeadTime currentLeadTime) {
        return of(LeadTimeMetrics.builder()
                .average(currentLeadTime.getAverageValue())
                .averageTendencyPercentage(EMPTY_PERCENTAGE)
                .averageCodingTime(currentLeadTime.getAverageCodingTime())
                .averageCodingTimePercentageTendency(EMPTY_PERCENTAGE)
                .averageReviewLag(currentLeadTime.getAverageReviewLag())
                .averageReviewLagPercentageTendency(EMPTY_PERCENTAGE)
                .averageReviewTime(currentLeadTime.getAverageReviewTime())
                .averageReviewTimePercentageTendency(EMPTY_PERCENTAGE)
                .build());
    }

    private static Optional<LeadTimeMetrics> getLeadTimeMetricsForCurrentAndPreviousLeadTimePresents(LeadTime currentLeadTime, LeadTime previousLeadTime) {
        return of(LeadTimeMetrics.builder()
                .average(currentLeadTime.getAverageValue())
                .averageTendencyPercentage(
                        getTendencyPercentage(currentLeadTime.getAverageValue(), previousLeadTime.getAverageValue())
                )
                .averageCodingTime(currentLeadTime.getAverageCodingTime())
                .averageCodingTimePercentageTendency(
                        getTendencyPercentage(currentLeadTime.getAverageCodingTime(),
                                previousLeadTime.getAverageCodingTime())
                )
                .averageReviewLag(currentLeadTime.getAverageReviewLag())
                .averageReviewLagPercentageTendency(
                        getTendencyPercentage(currentLeadTime.getAverageReviewLag(),
                                previousLeadTime.getAverageReviewLag())
                )
                .averageReviewTime(currentLeadTime.getAverageReviewTime())
                .averageReviewTimePercentageTendency(
                        getTendencyPercentage(currentLeadTime.getAverageReviewTime(),
                                previousLeadTime.getAverageReviewTime())
                )
                .build());
    }

    private static Float getTendencyPercentage(final Float currentValue, final Float previousValue) {
        return round(previousValue == 0 ? 0 : 1000 * (currentValue - previousValue) / previousValue) / 10f;
    }

}
