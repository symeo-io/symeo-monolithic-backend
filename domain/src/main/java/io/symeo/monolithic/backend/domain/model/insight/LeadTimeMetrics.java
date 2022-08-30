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


    public static Optional<LeadTimeMetrics> buildFromCurrentAndPreviousLeadTimes(final Optional<AverageLeadTime> optionalCurrentLeadTime,
                                                                                 final Optional<AverageLeadTime> optionalPreviousLeadTime) {
        if (optionalPreviousLeadTime.isEmpty() && optionalCurrentLeadTime.isEmpty()) {
            return empty();
        } else if (optionalCurrentLeadTime.isPresent() && optionalPreviousLeadTime.isPresent()) {
            final AverageLeadTime currentAverageLeadTime = optionalCurrentLeadTime.get();
            final AverageLeadTime previousAverageLeadTime = optionalPreviousLeadTime.get();
            return getLeadTimeMetricsForCurrentAndPreviousLeadTimePresents(currentAverageLeadTime, previousAverageLeadTime);
        } else if (optionalCurrentLeadTime.isPresent()) {
            final AverageLeadTime currentAverageLeadTime = optionalCurrentLeadTime.get();
            return getLeadTimeMetricsWithNoPreviousLeadTime(currentAverageLeadTime);
        } else {
            return empty();
        }
    }

    private static Optional<LeadTimeMetrics> getLeadTimeMetricsWithNoPreviousLeadTime(AverageLeadTime currentAverageLeadTime) {
        return of(LeadTimeMetrics.builder()
                .average(currentAverageLeadTime.getAverageValue())
                .averageTendencyPercentage(EMPTY_PERCENTAGE)
                .averageCodingTime(currentAverageLeadTime.getAverageCodingTime())
                .averageCodingTimePercentageTendency(EMPTY_PERCENTAGE)
                .averageReviewLag(currentAverageLeadTime.getAverageReviewLag())
                .averageReviewLagPercentageTendency(EMPTY_PERCENTAGE)
                .averageReviewTime(currentAverageLeadTime.getAverageReviewTime())
                .averageReviewTimePercentageTendency(EMPTY_PERCENTAGE)
                .build());
    }

    private static Optional<LeadTimeMetrics> getLeadTimeMetricsForCurrentAndPreviousLeadTimePresents(AverageLeadTime currentAverageLeadTime, AverageLeadTime previousAverageLeadTime) {
        return of(LeadTimeMetrics.builder()
                .average(currentAverageLeadTime.getAverageValue())
                .averageTendencyPercentage(
                        getTendencyPercentage(currentAverageLeadTime.getAverageValue(), previousAverageLeadTime.getAverageValue())
                )
                .averageCodingTime(currentAverageLeadTime.getAverageCodingTime())
                .averageCodingTimePercentageTendency(
                        getTendencyPercentage(currentAverageLeadTime.getAverageCodingTime(),
                                previousAverageLeadTime.getAverageCodingTime())
                )
                .averageReviewLag(currentAverageLeadTime.getAverageReviewLag())
                .averageReviewLagPercentageTendency(
                        getTendencyPercentage(currentAverageLeadTime.getAverageReviewLag(),
                                previousAverageLeadTime.getAverageReviewLag())
                )
                .averageReviewTime(currentAverageLeadTime.getAverageReviewTime())
                .averageReviewTimePercentageTendency(
                        getTendencyPercentage(currentAverageLeadTime.getAverageReviewTime(),
                                previousAverageLeadTime.getAverageReviewTime())
                )
                .build());
    }

    private static Float getTendencyPercentage(final Float currentValue, final Float previousValue) {
        return round(previousValue == 0 ? 0 : 1000 * (currentValue - previousValue) / previousValue) / 10f;
    }

}
