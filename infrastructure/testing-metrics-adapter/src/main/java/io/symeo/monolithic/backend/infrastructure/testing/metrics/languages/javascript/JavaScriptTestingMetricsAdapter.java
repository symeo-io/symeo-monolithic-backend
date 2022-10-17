package io.symeo.monolithic.backend.infrastructure.testing.metrics.languages.javascript;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.testing.metrics.languages.AbstractLanguageTestingMetricsAdapter;
import io.symeo.monolithic.backend.infrastructure.testing.metrics.languages.LanguageTestingMetricAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class JavaScriptTestingMetricsAdapter extends AbstractLanguageTestingMetricsAdapter implements LanguageTestingMetricAdapter {
    private final String[] testFilesSuffixes = {"test.ts", "test.tsx", "spec.ts", "spec.tsx", "test.js", "spec.js", "test.jsx", "spec.jsx"};
    private final String[] ignoredFolders = {"node_modules", "dist", "build"};
    private final String[] codeFilesSuffixes ={".ts", ".tsx", ".js", ".jsx"};
    private final Pattern testRegex = Pattern.compile("^\\s*(it|test)\\(['\"](.*)['\"]", Pattern.MULTILINE);

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
