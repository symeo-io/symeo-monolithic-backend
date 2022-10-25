package domain.model.insight;

import io.symeo.monolithic.backend.domain.bff.model.metric.DataCompareToLimit;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DataCompareToLimitTest {

    @Test
    void should_increment_immutable_values() {
        // Given
        DataCompareToLimit dataCompareToLimit = DataCompareToLimit.builder()
                .dateAsString("fake-date")
                .build();

        // When
        dataCompareToLimit = dataCompareToLimit.incrementDataAboveLimit();
        dataCompareToLimit = dataCompareToLimit.incrementDataAboveLimit();
        dataCompareToLimit = dataCompareToLimit.incrementDataAboveLimit();
        dataCompareToLimit = dataCompareToLimit.incrementDataBelowLimit();
        dataCompareToLimit = dataCompareToLimit.incrementDataBelowLimit();

        // Then
        assertThat(dataCompareToLimit.getNumberAboveLimit()).isEqualTo(3);
        assertThat(dataCompareToLimit.getNumberBelowLimit()).isEqualTo(2);
    }
}
