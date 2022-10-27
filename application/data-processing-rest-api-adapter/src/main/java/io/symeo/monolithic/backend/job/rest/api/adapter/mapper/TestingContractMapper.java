package io.symeo.monolithic.backend.job.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.data.processing.contract.api.model.CollectTestingDataRequestContract;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.testing.CommitTestingData;
import io.symeo.monolithic.backend.job.domain.testing.CoverageReportAdapter;

public interface TestingContractMapper {
    static CommitTestingData contractToDomain(CollectTestingDataRequestContract collectTestingDataRequestContract) throws SymeoException {
        return CommitTestingData.builder()
                .coverage(CoverageReportAdapter.extractCoverageFromReport(collectTestingDataRequestContract.getCoverageReport(), collectTestingDataRequestContract.getCoverageReportType()))
                .testLineCount(collectTestingDataRequestContract.getTestLineCount())
                .codeLineCount(collectTestingDataRequestContract.getCodeLineCount())
                .testCount(collectTestingDataRequestContract.getTestCount())
                .testType(collectTestingDataRequestContract.getTestType())
                .repositoryName(collectTestingDataRequestContract.getRepositoryName())
                .branchName(collectTestingDataRequestContract.getBranchName())
                .commitSha(collectTestingDataRequestContract.getCommitSha())
                .build();
    }
}
