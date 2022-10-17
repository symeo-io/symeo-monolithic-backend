package io.symeo.monolithic.backend.infrastructure.testing.metrics.languages.javascript;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JavaScriptTestingMetricsAdapterTest {
    @Test
    void should_count_the_number_of_javascript_tests() throws SymeoException {
        JavaScriptTestingMetricsAdapter javaScriptTestingMetricsAdapter = new JavaScriptTestingMetricsAdapter();
        Integer count = javaScriptTestingMetricsAdapter.getTestCount("src/test/resources/TestRepository");
        assertThat(count).isEqualTo(82);
    }

    @Test
    void should_count_the_number_of_javascript_test_lines() throws SymeoException {
        JavaScriptTestingMetricsAdapter javaScriptTestingMetricsAdapter = new JavaScriptTestingMetricsAdapter();
        Integer count = javaScriptTestingMetricsAdapter.getTestLineCount("src/test/resources/TestRepository");
        assertThat(count).isEqualTo(1804);
    }

    @Test
    void should_count_the_number_of_javascript_lines() throws SymeoException {
        JavaScriptTestingMetricsAdapter javaScriptTestingMetricsAdapter = new JavaScriptTestingMetricsAdapter();
        Integer count = javaScriptTestingMetricsAdapter.getTotalCodeLineCount("src/test/resources/TestRepository");
        assertThat(count).isEqualTo(2395);
    }
}
