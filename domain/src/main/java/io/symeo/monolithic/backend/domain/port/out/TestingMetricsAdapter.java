package io.symeo.monolithic.backend.domain.port.out;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.testing.TestToCodeRatio;

public interface TestingMetricsAdapter {
    Integer getRepositoryTestCount(String repositoryFilesPath, String commitSha) throws SymeoException;
    TestToCodeRatio getRepositoryTestToCodeRatio(String repositoryFilesPath, String commitSha) throws SymeoException;
}
