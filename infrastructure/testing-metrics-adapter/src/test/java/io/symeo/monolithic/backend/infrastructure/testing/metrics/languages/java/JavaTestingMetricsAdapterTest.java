package io.symeo.monolithic.backend.infrastructure.testing.metrics.languages.java;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JavaTestingMetricsAdapterTest {
    @Test
    void should_count_the_number_of_java_tests() throws SymeoException {
        JavaTestingMetricsAdapter javaTestingMetricsAdapter = new JavaTestingMetricsAdapter();
        Integer count = javaTestingMetricsAdapter.getTestCount("src/test/resources/TestRepository");
        assertThat(count).isEqualTo(9);
    }

    @Test
    void should_count_the_number_of_java_test_lines() throws SymeoException {
        JavaTestingMetricsAdapter javaTestingMetricsAdapter = new JavaTestingMetricsAdapter();
        Integer count = javaTestingMetricsAdapter.getTestLineCount("src/test/resources/TestRepository");
        assertThat(count).isEqualTo(286);
    }

    @Test
    void should_count_the_number_of_java_lines() throws SymeoException {
        JavaTestingMetricsAdapter javaTestingMetricsAdapter = new JavaTestingMetricsAdapter();
        Integer count = javaTestingMetricsAdapter.getTotalCodeLineCount("src/test/resources/TestRepository");
        assertThat(count).isEqualTo(452);
    }
}
