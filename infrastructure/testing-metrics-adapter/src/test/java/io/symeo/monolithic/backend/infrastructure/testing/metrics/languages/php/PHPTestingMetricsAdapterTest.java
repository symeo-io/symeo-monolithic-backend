package io.symeo.monolithic.backend.infrastructure.testing.metrics.languages.php;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PHPTestingMetricsAdapterTest {
    @Test
    void should_count_the_number_of_php_tests() throws SymeoException {
        PHPTestingMetricsAdapter phpTestingMetricsAdapter = new PHPTestingMetricsAdapter();
        Integer count = phpTestingMetricsAdapter.getTestCount("src/test/resources/TestRepository");
        assertThat(count).isEqualTo(2);
    }

    @Test
    void should_count_the_number_of_php_test_lines() throws SymeoException {
        PHPTestingMetricsAdapter phpTestingMetricsAdapter = new PHPTestingMetricsAdapter();
        Integer count = phpTestingMetricsAdapter.getTestLineCount("src/test/resources/TestRepository");
        assertThat(count).isEqualTo(41);
    }

    @Test
    void should_count_the_number_of_php_lines() throws SymeoException {
        PHPTestingMetricsAdapter phpTestingMetricsAdapter = new PHPTestingMetricsAdapter();
        Integer count = phpTestingMetricsAdapter.getTotalCodeLineCount("src/test/resources/TestRepository");
        assertThat(count).isEqualTo(146);
    }
}
