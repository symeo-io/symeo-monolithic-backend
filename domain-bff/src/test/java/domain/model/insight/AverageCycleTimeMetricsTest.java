package domain.model.insight;

import io.symeo.monolithic.backend.domain.bff.model.metric.AverageCycleTime;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTimeMetrics;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Optional;

import static io.symeo.monolithic.backend.domain.bff.model.metric.CycleTimeMetrics.buildFromCurrentAndPreviousCycleTimes;
import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.assertj.core.api.Assertions.assertThat;

public class AverageCycleTimeMetricsTest {

    @Test
    void should_compute_cycle_time_metrics_given_current_and_previous_average_cycle_time() throws SymeoException {
        // Given
        final Optional<AverageCycleTime> currentCycleTime = Optional.of(
                AverageCycleTime.builder()
                        .averageValue(123.0f)
                        .averageCodingTime(98.5f)
                        .averageReviewTime(10.2f)
                        .averageDeployTime(11.3f)
                        .build()
        );
        final Optional<AverageCycleTime> previousCycleTime = Optional.of(
                AverageCycleTime.builder()
                        .averageValue(145.0f)
                        .averageCodingTime(55.5f)
                        .averageReviewTime(66.2f)
                        .averageDeployTime(2.4f)
                        .build()
        );
        final Date previousStartDate = stringToDate("2020-01-01");
        final Date currentStartDate = stringToDate("2020-01-02");
        final Date currentEndDate = stringToDate("2020-01-03");


        // When
        final Optional<CycleTimeMetrics> optionalCycleTimeMetrics = buildFromCurrentAndPreviousCycleTimes(
                currentCycleTime,
                previousCycleTime,
                previousStartDate,
                currentStartDate,
                currentEndDate
        );

        // Then
        assertThat(optionalCycleTimeMetrics).isPresent();
        final CycleTimeMetrics cycleTimeMetrics = optionalCycleTimeMetrics.get();
        assertThat(cycleTimeMetrics.getAverage()).isEqualTo(123.0f);
        assertThat(cycleTimeMetrics.getAverageTendencyPercentage()).isEqualTo(-15.2f);
        assertThat(cycleTimeMetrics.getAverageCodingTime()).isEqualTo(98.5f);
        assertThat(cycleTimeMetrics.getAverageCodingTimePercentageTendency()).isEqualTo(77.5f);
        assertThat(cycleTimeMetrics.getAverageReviewTime()).isEqualTo(10.2f);
        assertThat(cycleTimeMetrics.getAverageReviewTimePercentageTendency()).isEqualTo(-84.6f);
        assertThat(cycleTimeMetrics.getAverageDeployTime()).isEqualTo(11.3f);
        assertThat(cycleTimeMetrics.getAverageDeployTimePercentageTendency()).isEqualTo(370.8f);
    }

    @Test
    void should_compute_cycle_time_given_no_previous_cycle_time() throws SymeoException {
        // Given
        final Optional<AverageCycleTime> currentCycleTime = Optional.of(
                AverageCycleTime.builder()
                        .averageValue(123.0f)
                        .averageCodingTime(98.5f)
                        .averageReviewTime(10.2f)
                        .build()
        );
        final Optional<AverageCycleTime> previousCycleTime = Optional.empty();
        final Date previousStartDate = stringToDate("2020-01-01");
        final Date currentStartDate = stringToDate("2020-01-02");
        final Date currentEndDate = stringToDate("2020-01-03");

        // When
        final Optional<CycleTimeMetrics> optionalCycleTimeMetrics = buildFromCurrentAndPreviousCycleTimes(
                currentCycleTime,
                previousCycleTime,
                previousStartDate,
                currentStartDate,
                currentEndDate
        );

        // Then
        assertThat(optionalCycleTimeMetrics).isPresent();
        final CycleTimeMetrics cycleTimeMetrics = optionalCycleTimeMetrics.get();
        assertThat(cycleTimeMetrics.getAverage()).isEqualTo(123.0f);
        assertThat(cycleTimeMetrics.getAverageTendencyPercentage()).isEqualTo(0.0f);
        assertThat(cycleTimeMetrics.getAverageCodingTime()).isEqualTo(98.5f);
        assertThat(cycleTimeMetrics.getAverageCodingTimePercentageTendency()).isEqualTo(0.0f);
        assertThat(cycleTimeMetrics.getAverageReviewTime()).isEqualTo(10.2f);
        assertThat(cycleTimeMetrics.getAverageReviewTimePercentageTendency()).isEqualTo(0.0f);
    }

    @Test
    void should_no_compute_cycle_time_for_empty_cycle_times() throws SymeoException {
        // Given
        final Optional<AverageCycleTime> previousCycleTime = Optional.of(
                AverageCycleTime.builder()
                        .averageValue(123.0f)
                        .averageCodingTime(98.5f)
                        .averageReviewTime(10.2f)
                        .build()
        );
        final Date previousStartDate = stringToDate("2020-01-01");
        final Date currentStartDate = stringToDate("2020-01-02");
        final Date currentEndDate = stringToDate("2020-01-03");

        // When
        final Optional<CycleTimeMetrics> optionalCycleTimeMetrics1 =
                buildFromCurrentAndPreviousCycleTimes(Optional.empty(), previousCycleTime, previousStartDate,
                        currentStartDate,
                        currentEndDate);
        final Optional<CycleTimeMetrics> optionalCycleTimeMetrics2 =
                buildFromCurrentAndPreviousCycleTimes(Optional.empty(), Optional.empty(), previousStartDate,
                        currentStartDate,
                        currentEndDate);

        // Then
        assertThat(optionalCycleTimeMetrics1).isEmpty();
        assertThat(optionalCycleTimeMetrics2).isEmpty();
    }

    @Test
    void should_compute_average_with_null_values() throws SymeoException {
        // Given
        final Optional<AverageCycleTime> currentCycleTime = Optional.of(
                AverageCycleTime.builder()
                        .averageValue(null)
                        .averageCodingTime(null)
                        .averageReviewTime(null)
                        .averageDeployTime(null)
                        .build()
        );
        final Optional<AverageCycleTime> previousCycleTime = Optional.of(
                AverageCycleTime.builder()
                        .averageValue(145.0f)
                        .averageCodingTime(55.5f)
                        .averageReviewTime(66.2f)
                        .averageDeployTime(2.4f)
                        .build()
        );
        final Date previousStartDate = stringToDate("2020-01-01");
        final Date currentStartDate = stringToDate("2020-01-02");
        final Date currentEndDate = stringToDate("2020-01-03");


        // When
        final Optional<CycleTimeMetrics> optionalCycleTimeMetrics = buildFromCurrentAndPreviousCycleTimes(
                currentCycleTime,
                previousCycleTime,
                previousStartDate,
                currentStartDate,
                currentEndDate
        );

        // Then
        assertThat(optionalCycleTimeMetrics).isPresent();
        final CycleTimeMetrics cycleTimeMetrics = optionalCycleTimeMetrics.get();
        assertThat(cycleTimeMetrics.getAverage()).isNull();
        assertThat(cycleTimeMetrics.getAverageTendencyPercentage()).isNull();
        assertThat(cycleTimeMetrics.getAverageCodingTime()).isNull();
        assertThat(cycleTimeMetrics.getAverageCodingTimePercentageTendency()).isNull();
        assertThat(cycleTimeMetrics.getAverageReviewTime()).isNull();
        assertThat(cycleTimeMetrics.getAverageReviewTimePercentageTendency()).isNull();
        assertThat(cycleTimeMetrics.getAverageDeployTime()).isNull();
        assertThat(cycleTimeMetrics.getAverageDeployTimePercentageTendency()).isNull();
    }
}
