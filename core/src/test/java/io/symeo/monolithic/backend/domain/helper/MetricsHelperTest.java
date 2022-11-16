package io.symeo.monolithic.backend.domain.helper;

import org.junit.jupiter.api.Test;

import static io.symeo.monolithic.backend.domain.helper.MetricsHelper.*;
import static org.assertj.core.api.Assertions.assertThat;

public class MetricsHelperTest {

    @Test
    void should_return_tendency_percentage() {
        // Given
        final Float currentValue = 88f;
        final Float previousValue = 63f;

        // When
        final Float tendencyPercentage = getTendencyPercentage(currentValue, previousValue);
        final Float tendencyPercentageWithEmptyPreviousValue = getTendencyPercentage(currentValue, null);
        final Float tendencyPercentageWithEmptyCurrentValue = getTendencyPercentage(null, previousValue);
        final Float tendencyPercentageWithEmptyCurrentAndPreviousValue = getTendencyPercentage((Float) null, null);

        // Then
        assertThat(tendencyPercentage).isEqualTo(39.7f);
        assertThat(tendencyPercentageWithEmptyPreviousValue).isNull();
        assertThat(tendencyPercentageWithEmptyCurrentValue).isNull();
        assertThat(tendencyPercentageWithEmptyCurrentAndPreviousValue).isNull();
    }
}
