package io.symeo.monolithic.backend.domain.model.insight;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Optional;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static io.symeo.monolithic.backend.domain.model.insight.LeadTimeMetrics.buildFromCurrentAndPreviousLeadTimes;
import static org.assertj.core.api.Assertions.assertThat;

public class AverageLeadTimeMetricsTest {

    @Test
    void should_compute_lead_time_metrics_given_current_and_previous_average_lead_time() throws SymeoException {
        // Given
        final Optional<AverageLeadTime> currentLeadTime = Optional.of(
                AverageLeadTime.builder()
                        .averageValue(123.0f)
                        .averageCodingTime(98.5f)
                        .averageReviewLag(14.3f)
                        .averageReviewTime(10.2f)
                        .build()
        );
        final Optional<AverageLeadTime> previousLeadTime = Optional.of(
                AverageLeadTime.builder()
                        .averageValue(145.0f)
                        .averageCodingTime(55.5f)
                        .averageReviewLag(23.3f)
                        .averageReviewTime(66.2f)
                        .build()
        );
        final Date previousStartDate = stringToDate("2020-01-01");
        final Date currentStartDate = stringToDate("2020-01-02");
        final Date currentEndDate = stringToDate("2020-01-03");


        // When
        final Optional<LeadTimeMetrics> optionalLeadTimeMetrics = buildFromCurrentAndPreviousLeadTimes(
                currentLeadTime,
                previousLeadTime,
                previousStartDate,
                currentStartDate,
                currentEndDate
        );

        // Then
        assertThat(optionalLeadTimeMetrics).isPresent();
        final LeadTimeMetrics leadTimeMetrics = optionalLeadTimeMetrics.get();
        assertThat(leadTimeMetrics.getAverage()).isEqualTo(123.0f);
        assertThat(leadTimeMetrics.getAverageTendencyPercentage()).isEqualTo(-15.2f);
        assertThat(leadTimeMetrics.getAverageCodingTime()).isEqualTo(98.5f);
        assertThat(leadTimeMetrics.getAverageCodingTimePercentageTendency()).isEqualTo(77.5f);
        assertThat(leadTimeMetrics.getAverageReviewLag()).isEqualTo(14.3f);
        assertThat(leadTimeMetrics.getAverageReviewLagPercentageTendency()).isEqualTo(-38.6f);
        assertThat(leadTimeMetrics.getAverageReviewTime()).isEqualTo(10.2f);
        assertThat(leadTimeMetrics.getAverageReviewTimePercentageTendency()).isEqualTo(-84.6f);
    }

    @Test
    void should_compute_lead_time_given_no_previous_lead_time() throws SymeoException {
        // Given
        final Optional<AverageLeadTime> currentLeadTime = Optional.of(
                AverageLeadTime.builder()
                        .averageValue(123.0f)
                        .averageCodingTime(98.5f)
                        .averageReviewLag(14.3f)
                        .averageReviewTime(10.2f)
                        .build()
        );
        final Optional<AverageLeadTime> previousLeadTime = Optional.empty();
        final Date previousStartDate = stringToDate("2020-01-01");
        final Date currentStartDate = stringToDate("2020-01-02");
        final Date currentEndDate = stringToDate("2020-01-03");

        // When
        final Optional<LeadTimeMetrics> optionalLeadTimeMetrics = buildFromCurrentAndPreviousLeadTimes(
                currentLeadTime,
                previousLeadTime,
                previousStartDate,
                currentStartDate,
                currentEndDate
        );

        // Then
        assertThat(optionalLeadTimeMetrics).isPresent();
        final LeadTimeMetrics leadTimeMetrics = optionalLeadTimeMetrics.get();
        assertThat(leadTimeMetrics.getAverage()).isEqualTo(123.0f);
        assertThat(leadTimeMetrics.getAverageTendencyPercentage()).isEqualTo(0.0f);
        assertThat(leadTimeMetrics.getAverageCodingTime()).isEqualTo(98.5f);
        assertThat(leadTimeMetrics.getAverageCodingTimePercentageTendency()).isEqualTo(0.0f);
        assertThat(leadTimeMetrics.getAverageReviewLag()).isEqualTo(14.3f);
        assertThat(leadTimeMetrics.getAverageReviewLagPercentageTendency()).isEqualTo(0.0f);
        assertThat(leadTimeMetrics.getAverageReviewTime()).isEqualTo(10.2f);
        assertThat(leadTimeMetrics.getAverageReviewTimePercentageTendency()).isEqualTo(0.0f);
    }

    @Test
    void should_no_compute_lead_time_for_empty_lead_times() throws SymeoException {
        // Given
        final Optional<AverageLeadTime> previousLeadTime = Optional.of(
                AverageLeadTime.builder()
                        .averageValue(123.0f)
                        .averageCodingTime(98.5f)
                        .averageReviewLag(14.3f)
                        .averageReviewTime(10.2f)
                        .build()
        );
        final Date previousStartDate = stringToDate("2020-01-01");
        final Date currentStartDate = stringToDate("2020-01-02");
        final Date currentEndDate = stringToDate("2020-01-03");

        // When
        final Optional<LeadTimeMetrics> optionalLeadTimeMetrics1 =
                buildFromCurrentAndPreviousLeadTimes(Optional.empty(), previousLeadTime, previousStartDate,
                        currentStartDate,
                        currentEndDate);
        final Optional<LeadTimeMetrics> optionalLeadTimeMetrics2 =
                buildFromCurrentAndPreviousLeadTimes(Optional.empty(), Optional.empty(), previousStartDate,
                        currentStartDate,
                        currentEndDate);

        // Then
        assertThat(optionalLeadTimeMetrics1).isEmpty();
        assertThat(optionalLeadTimeMetrics2).isEmpty();
    }
}
