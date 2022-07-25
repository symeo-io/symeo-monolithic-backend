package catlean.monolithic.backend.rest.api.adapter;

import catlean.monolithic.backend.rest.api.adapter.validator.PullRequestHistogramValidator;
import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.exception.CatleanExceptionCode;
import fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PullRequestHistogramValidatorTest {

    @Test
    void should_validate_histogram_type() throws CatleanException {
        // Given
        String histogramType = PullRequestHistogram.SIZE_LIMIT;
        // When
        PullRequestHistogramValidator.validate(histogramType);
        histogramType = PullRequestHistogram.TIME_LIMIT;
        PullRequestHistogramValidator.validate(histogramType);
    }

    @Test
    void should_invalidate_wrong_histogram_type() {
        // Given
        CatleanException exception = null;

        try {
            // When
            PullRequestHistogramValidator.validate(new Faker().dragonBall().character());
        } catch (CatleanException e) {
            exception = e;

        }

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(CatleanExceptionCode.INVALID_HISTOGRAM_TYPE);
        assertThat(exception.getMessage()).isEqualTo("Invalid histogram type");
    }
}
