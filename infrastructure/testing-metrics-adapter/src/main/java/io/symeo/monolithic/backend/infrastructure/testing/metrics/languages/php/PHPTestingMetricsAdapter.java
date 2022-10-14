package io.symeo.monolithic.backend.infrastructure.testing.metrics.languages.php;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.testing.metrics.languages.AbstractLanguageTestingMetricsAdapter;
import io.symeo.monolithic.backend.infrastructure.testing.metrics.languages.LanguageTestingMetricAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class PHPTestingMetricsAdapter extends AbstractLanguageTestingMetricsAdapter implements LanguageTestingMetricAdapter {
    private final String[] testFilesSuffixes = {"Test.php"};
    private final String[] ignoredFolders = {"vendor"};
    private final String[] codeFilesSuffixes ={".php"};
    private final Pattern testRegex = Pattern.compile("function (test.*)\\(\\)$", Pattern.MULTILINE);

    @Override
    public Integer getTestCount(String repositoryFilesPath) throws SymeoException {
        List<String> testFilesPaths = this.getFiles(repositoryFilesPath, this.testFilesSuffixes, this.ignoredFolders);
        return this.getPatternCountInFiles(testFilesPaths, this.testRegex);
    }
    @Override
    public Integer getTestLineCount(String repositoryFilesPath) throws SymeoException {
        List<String> testFilesPaths = this.getFiles(repositoryFilesPath, this.testFilesSuffixes, this.ignoredFolders);
        return this.getLineCountForFilePaths(testFilesPaths);
    }
    @Override
    public Integer getTotalCodeLineCount(String repositoryFilesPath) throws SymeoException {
        List<String> codeFilesPaths = this.getFiles(repositoryFilesPath, this.codeFilesSuffixes, this.ignoredFolders);
        return this.getLineCountForFilePaths(codeFilesPaths);
    }
}
