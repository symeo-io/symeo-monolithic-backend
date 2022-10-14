package io.symeo.monolithic.backend.infrastructure.testing.metrics;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.testing.TestToCodeRatio;
import io.symeo.monolithic.backend.domain.port.out.TestingMetricsAdapter;
import io.symeo.monolithic.backend.infrastructure.testing.metrics.languages.LanguageTestingMetricAdapter;
import io.symeo.monolithic.backend.infrastructure.testing.metrics.languages.java.JavaTestingMetricsAdapter;
import io.symeo.monolithic.backend.infrastructure.testing.metrics.languages.javascript.JavaScriptTestingMetricsAdapter;
import io.symeo.monolithic.backend.infrastructure.testing.metrics.languages.php.PHPTestingMetricsAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;

import static io.symeo.monolithic.backend.domain.exception.SymeoException.getSymeoException;
import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.GIT_FAILED_TO_OPEN_REPOSITORY;

@AllArgsConstructor
@Slf4j
public class AllLanguageTestingMetricsAdapter implements TestingMetricsAdapter {
    private static final String[] supportedLanguages = {"Java", "PHP", "JavaScript", "Python"};
    private JavaTestingMetricsAdapter javaTestingMetricsAdapter;
    private JavaScriptTestingMetricsAdapter javaScriptTestingMetricsAdapter;
    private PHPTestingMetricsAdapter phpTestingMetricsAdapter;

    @Override
    public Integer getRepositoryTestCount(String repositoryFilesPath, String commitSha) throws SymeoException {
        this.checkoutRepositoryToCommit(repositoryFilesPath, commitSha);

        Integer results = 0;
        for (String language : supportedLanguages) {
            results += this.getLanguageTestingMetricAdapter(language).getTestCount(repositoryFilesPath);
        }
        return results;
    }

    @Override
    public TestToCodeRatio getRepositoryTestToCodeRatio(String repositoryFilesPath, String commitSha) throws SymeoException {
        this.checkoutRepositoryToCommit(repositoryFilesPath, commitSha);
        Integer totalCodeLines = 0;
        Integer testCodeLines = 0;

        for (String language : supportedLanguages) {
            totalCodeLines += this.getLanguageTestingMetricAdapter(language).getTotalCodeLineCount(repositoryFilesPath);
            testCodeLines += this.getLanguageTestingMetricAdapter(language).getTestLineCount(repositoryFilesPath);
        }

        return TestToCodeRatio.builder().testCodeLines(testCodeLines).totalCodeLines(totalCodeLines).build();
    }

    private void checkoutRepositoryToCommit(String repositoryFilesPath, String commitSha) throws SymeoException {
        try {
            File gitDirectory = new File(repositoryFilesPath + "/.git");
            Git.open(gitDirectory).checkout().setName(commitSha).call();
        } catch (IOException | GitAPIException e) {
            throw getSymeoException("Failed to open git repository folder at " + repositoryFilesPath + " with commit " + commitSha, GIT_FAILED_TO_OPEN_REPOSITORY);
        }
    }

    private LanguageTestingMetricAdapter getLanguageTestingMetricAdapter(String language) {
        switch (language) {
            case "Java":
                return this.javaTestingMetricsAdapter;
            case "JavaScript":
                return this.javaScriptTestingMetricsAdapter;
            case "PHP":
                return this.phpTestingMetricsAdapter;
            default:
                return this.javaTestingMetricsAdapter;
        }
    }
}
