package io.symeo.monolithic.backend.infrastructure.testing.metrics.languages.java;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.testing.metrics.languages.AbstractLanguageTestingMetricsAdapter;
import io.symeo.monolithic.backend.infrastructure.testing.metrics.languages.LanguageTestingMetricAdapter;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class JavaTestingMetricsAdapter extends AbstractLanguageTestingMetricsAdapter implements LanguageTestingMetricAdapter {
    private final String[] testFilesSuffixes = {"Test.java"};
    private final String[] ignoredFolders = {"target", ".git"};
    private final String[] codeFilesSuffixes ={".java"};
    private final Pattern testRegex = Pattern.compile("@Test\n.*?void (\\S+)\\(\\)", Pattern.MULTILINE);

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
