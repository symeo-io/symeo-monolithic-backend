package io.symeo.monolithic.backend.infrastructure.testing.metrics;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.testing.TestToCodeRatio;
import io.symeo.monolithic.backend.infrastructure.testing.metrics.languages.java.JavaTestingMetricsAdapter;
import io.symeo.monolithic.backend.infrastructure.testing.metrics.languages.javascript.JavaScriptTestingMetricsAdapter;
import io.symeo.monolithic.backend.infrastructure.testing.metrics.languages.php.PHPTestingMetricsAdapter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AllLanguageTestingMetricsAdapterTest {

    @Test
    void should_switch_repository_branch() throws SymeoException {
        JavaTestingMetricsAdapter javaTestingMetricsAdapter = new JavaTestingMetricsAdapter();
        JavaScriptTestingMetricsAdapter javaScriptTestingMetricsAdapter = new JavaScriptTestingMetricsAdapter();
        PHPTestingMetricsAdapter phpTestingMetricsAdapter = new PHPTestingMetricsAdapter();
        AllLanguageTestingMetricsAdapter testingMetricsAdapter = new AllLanguageTestingMetricsAdapter(javaTestingMetricsAdapter, javaScriptTestingMetricsAdapter, phpTestingMetricsAdapter);
        Integer result = testingMetricsAdapter.getRepositoryTestCount("/Users/georgesbiaux/Projects/Catlean/catlean-monolithic-backend", "c1bb0b399b39bd62a324b869f0f21430dc07ac88");
        TestToCodeRatio testToCodeRatio = testingMetricsAdapter.getRepositoryTestToCodeRatio("/Users/georgesbiaux/Projects/Catlean/catlean-monolithic-backend", "c1bb0b399b39bd62a324b869f0f21430dc07ac88");
        System.out.println("result");
        System.out.println(result);
        System.out.println("testToCodeRatio");
        System.out.println(testToCodeRatio.getTestCodeLines());
        System.out.println(testToCodeRatio.getTotalCodeLines());
        System.out.println(testToCodeRatio.getRatio());

        assertThat(result).isNotNull();
    }
}
