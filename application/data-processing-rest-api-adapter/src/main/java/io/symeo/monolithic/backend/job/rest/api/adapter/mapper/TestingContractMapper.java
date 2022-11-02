package io.symeo.monolithic.backend.job.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.data.processing.contract.api.model.CollectTestingDataRequestContract;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.testing.CommitTestingData;
import io.symeo.monolithic.backend.job.domain.testing.CoverageReportAdapter;

import java.util.Date;

public interface TestingContractMapper {
    static CommitTestingData contractToDomain(CollectTestingDataRequestContract collectTestingDataRequestContract, Organization organization) throws SymeoException {
        return CommitTestingData.builder()
                .organizationId(organization.getId())
                .coverage(CoverageReportAdapter.extractCoverageFromReport(collectTestingDataRequestContract.getCoverageReport(), collectTestingDataRequestContract.getCoverageReportType()))
                .testLineCount(collectTestingDataRequestContract.getTestLineCount())
                .codeLineCount(collectTestingDataRequestContract.getCodeLineCount())
                .unitTestCount(collectTestingDataRequestContract.getUnitTestCount())
                .integrationTestCount(collectTestingDataRequestContract.getIntegrationTestCount())
                .repositoryName(collectTestingDataRequestContract.getRepositoryName())
                .branchName(collectTestingDataRequestContract.getBranchName())
                .commitSha(collectTestingDataRequestContract.getCommitSha())
                .date(new Date())
                .build();
    }
}
