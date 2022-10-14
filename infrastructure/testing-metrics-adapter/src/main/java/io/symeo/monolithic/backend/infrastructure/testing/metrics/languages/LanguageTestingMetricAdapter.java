package io.symeo.monolithic.backend.infrastructure.testing.metrics.languages;

import io.symeo.monolithic.backend.domain.exception.SymeoException;

public interface LanguageTestingMetricAdapter {
    public Integer getTestCount(String repositoryFilesPath) throws SymeoException;
    public Integer getTestLineCount(String repositoryFilesPath) throws SymeoException;
    public Integer getTotalCodeLineCount(String repositoryFilesPath) throws SymeoException;
}
