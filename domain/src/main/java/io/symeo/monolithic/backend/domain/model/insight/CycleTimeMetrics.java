package io.symeo.monolithic.backend.domain.model.insight;

import lombok.Builder;
import lombok.Value;

import java.util.Date;
import java.util.Optional;

import static java.lang.Math.round;
import static java.util.Objects.isNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;

@Value
@Builder
public class CycleTimeMetrics {
    private static final Float EMPTY_PERCENTAGE = 0.0f;
    Float average;
    Float averageTendencyPercentage;
    Float averageCodingTime;
    Float averageCodingTimePercentageTendency;
    Float averageReviewTime;
    Float averageReviewTimePercentageTendency;
    Float averageDeployTime;
    Float averageDeployTimePercentageTendency;
    Date currentStartDate;
    Date currentEndDate;
    Date previousStartDate;
    Date previousEndDate;


    public static Optional<CycleTimeMetrics> buildFromCurrentAndPreviousCycleTimes(final Optional<AverageCycleTime> optionalCurrentCycleTime,
                                                                                   final Optional<AverageCycleTime> optionalPreviousCycleTime,
                                                                                   final Date previousStartDate,
                                                                                   final Date currentStartDate,
                                                                                   final Date currentEndDate) {
        if (optionalPreviousCycleTime.isEmpty() && optionalCurrentCycleTime.isEmpty()) {
            return empty();
        } else if (optionalCurrentCycleTime.isPresent() && optionalPreviousCycleTime.isPresent()) {
            final AverageCycleTime currentAverageCycleTime = optionalCurrentCycleTime.get();
            final AverageCycleTime previousAverageCycleTime = optionalPreviousCycleTime.get();
            return getCycleTimeMetricsForCurrentAndPreviousCycleTimePresents(currentAverageCycleTime,
                    previousAverageCycleTime, previousStartDate, currentStartDate, currentEndDate);
        } else if (optionalCurrentCycleTime.isPresent()) {
            final AverageCycleTime currentAverageCycleTime = optionalCurrentCycleTime.get();
            return getCycleTimeMetricsWithNoPreviousCycleTime(currentAverageCycleTime, previousStartDate,
                    currentStartDate, currentEndDate);
        } else {
            return empty();
        }
    }

    private static Optional<CycleTimeMetrics> getCycleTimeMetricsWithNoPreviousCycleTime(final AverageCycleTime currentAverageCycleTime,
                                                                                         final Date previousStartDate,
                                                                                         final Date currentStartDate,
                                                                                         final Date currentEndDate) {
        return of(CycleTimeMetrics.builder()
                .average(currentAverageCycleTime.getAverageValue())
                .averageTendencyPercentage(EMPTY_PERCENTAGE)
                .averageCodingTime(currentAverageCycleTime.getAverageCodingTime())
                .averageCodingTimePercentageTendency(EMPTY_PERCENTAGE)
                .averageReviewTime(currentAverageCycleTime.getAverageReviewTime())
                .averageReviewTimePercentageTendency(EMPTY_PERCENTAGE)
                .averageDeployTime(currentAverageCycleTime.getAverageDeployTime())
                .averageDeployTimePercentageTendency(EMPTY_PERCENTAGE)
                .previousStartDate(previousStartDate)
                .previousEndDate(currentStartDate)
                .currentStartDate(currentStartDate)
                .currentEndDate(currentEndDate)
                .build());
    }

    private static Optional<CycleTimeMetrics> getCycleTimeMetricsForCurrentAndPreviousCycleTimePresents(final AverageCycleTime currentAverageCycleTime,
                                                                                                        final AverageCycleTime previousAverageCycleTime,
                                                                                                        final Date previousStartDate,
                                                                                                        final Date currentStartDate,
                                                                                                        final Date currentEndDate) {
        return of(CycleTimeMetrics.builder()
                .average(currentAverageCycleTime.getAverageValue())
                .averageTendencyPercentage(
                        getTendencyPercentage(currentAverageCycleTime.getAverageValue(),
                                previousAverageCycleTime.getAverageValue())
                )
                .averageCodingTime(currentAverageCycleTime.getAverageCodingTime())
                .averageCodingTimePercentageTendency(
                        getTendencyPercentage(currentAverageCycleTime.getAverageCodingTime(),
                                previousAverageCycleTime.getAverageCodingTime())
                )
                .averageReviewTime(currentAverageCycleTime.getAverageReviewTime())
                .averageReviewTimePercentageTendency(
                        getTendencyPercentage(currentAverageCycleTime.getAverageReviewTime(),
                                previousAverageCycleTime.getAverageReviewTime())
                )
                .averageDeployTime(currentAverageCycleTime.getAverageDeployTime())
                .averageDeployTimePercentageTendency(
                        getTendencyPercentage(currentAverageCycleTime.getAverageDeployTime(),
                                previousAverageCycleTime.getAverageDeployTime())
                )
                .previousStartDate(previousStartDate)
                .previousEndDate(currentStartDate)
                .currentStartDate(currentStartDate)
                .currentEndDate(currentEndDate)
                .build());
    }

    private static Float getTendencyPercentage(final Float currentValue, final Float previousValue) {
        if (isNull(currentValue) || isNull(previousValue)) {
            return null;
        }
        return round(previousValue == 0 ? 0 : 1000 * (currentValue - previousValue) / previousValue) / 10f;
    }

}
