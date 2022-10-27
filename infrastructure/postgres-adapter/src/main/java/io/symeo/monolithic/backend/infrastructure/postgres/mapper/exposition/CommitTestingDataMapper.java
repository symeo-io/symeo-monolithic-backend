package io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition;

import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.CommitTestingDataEntity;
import io.symeo.monolithic.backend.job.domain.model.testing.CommitTestingData;
import io.symeo.monolithic.backend.job.domain.testing.CoverageReportAdapter;

import java.util.UUID;

import static java.util.Objects.isNull;

public interface CommitTestingDataMapper {
    static CommitTestingDataEntity domainToEntity(final CommitTestingData commitTestingData) {
        return CommitTestingDataEntity.builder()
                .id(UUID.randomUUID())
                .organizationId(commitTestingData.getOrganizationId())
                .coverage(commitTestingData.getCoverage())
                .testLineCount(commitTestingData.getTestLineCount())
                .codeLineCount(commitTestingData.getCodeLineCount())
                .testCount(commitTestingData.getTestCount())
                .testType(commitTestingData.getTestType())
                .repositoryName(commitTestingData.getRepositoryName())
                .branchName(commitTestingData.getBranchName())
                .commitSha(commitTestingData.getCommitSha())
                .build();
    }
}
