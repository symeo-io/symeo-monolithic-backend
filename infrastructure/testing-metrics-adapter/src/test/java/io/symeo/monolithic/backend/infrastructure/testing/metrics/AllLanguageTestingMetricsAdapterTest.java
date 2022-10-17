package io.symeo.monolithic.backend.infrastructure.testing.metrics;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.testing.TestToCodeRatio;
import io.symeo.monolithic.backend.infrastructure.testing.metrics.languages.java.JavaTestingMetricsAdapter;
import io.symeo.monolithic.backend.infrastructure.testing.metrics.languages.javascript.JavaScriptTestingMetricsAdapter;
import io.symeo.monolithic.backend.infrastructure.testing.metrics.languages.php.PHPTestingMetricsAdapter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AllLanguageTestingMetricsAdapterTest {

    @Test
    void should_compute_test_count_and_switch_repository_branch() throws SymeoException {
        // Given
        final JavaTestingMetricsAdapter javaTestingMetricsAdapter = new JavaTestingMetricsAdapter();
        final JavaScriptTestingMetricsAdapter javaScriptTestingMetricsAdapter = new JavaScriptTestingMetricsAdapter();
        final PHPTestingMetricsAdapter phpTestingMetricsAdapter = new PHPTestingMetricsAdapter();
        final GitRepositoryService gitRepositoryService = mock(GitRepositoryService.class);

        final AllLanguageTestingMetricsAdapter testingMetricsAdapter = new AllLanguageTestingMetricsAdapter(javaTestingMetricsAdapter, javaScriptTestingMetricsAdapter, phpTestingMetricsAdapter, gitRepositoryService);

        // When
        Integer result = testingMetricsAdapter.getRepositoryTestCount("src/test/resources/TestRepository", "c1bb0b399b39bd62a324b869f0f21430dc07ac88");

        // Then
        verify(gitRepositoryService, times(1)).checkoutRepositoryToCommit("src/test/resources/TestRepository", "c1bb0b399b39bd62a324b869f0f21430dc07ac88");
        assertThat(result).isEqualTo(102);
    }

    @Test
    void should_compute_test_to_code_ratio_and_switch_repository_branch() throws SymeoException {
        // Given
        final JavaTestingMetricsAdapter javaTestingMetricsAdapter = new JavaTestingMetricsAdapter();
        final JavaScriptTestingMetricsAdapter javaScriptTestingMetricsAdapter = new JavaScriptTestingMetricsAdapter();
        final PHPTestingMetricsAdapter phpTestingMetricsAdapter = new PHPTestingMetricsAdapter();
        final GitRepositoryService gitRepositoryService = mock(GitRepositoryService.class);

        final AllLanguageTestingMetricsAdapter testingMetricsAdapter = new AllLanguageTestingMetricsAdapter(javaTestingMetricsAdapter, javaScriptTestingMetricsAdapter, phpTestingMetricsAdapter, gitRepositoryService);

        // When
        TestToCodeRatio result = testingMetricsAdapter.getRepositoryTestToCodeRatio("src/test/resources/TestRepository", "c1bb0b399b39bd62a324b869f0f21430dc07ac88");

        // Then
        verify(gitRepositoryService, times(1)).checkoutRepositoryToCommit("src/test/resources/TestRepository", "c1bb0b399b39bd62a324b869f0f21430dc07ac88");
        assertThat(result.getTestCodeLines()).isEqualTo(2417);
        assertThat(result.getTotalCodeLines()).isEqualTo(3445);
        assertThat(result.getRatio()).isEqualTo((float) 2417 / 3445);
    }
}
