package io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition;

import io.symeo.monolithic.backend.domain.bff.model.metric.CommitTestingDataView;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.CommitTestingDataEntity;
import io.symeo.monolithic.backend.job.domain.model.testing.CommitTestingData;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

import static java.util.Objects.isNull;

public interface CommitTestingDataMapper {
    static CommitTestingDataEntity domainToEntity(final CommitTestingData commitTestingData) {
        return CommitTestingDataEntity.builder()
                .id(UUID.randomUUID())
                .organizationId(commitTestingData.getOrganizationId())
                .coveredBranches(isNull(commitTestingData.getCoverage()) ? null :
                        commitTestingData.getCoverage().getCoveredBranches())
                .totalBranchCount(isNull(commitTestingData.getCoverage()) ? null :
                        commitTestingData.getCoverage().getTotalBranchCount())
                .testLineCount(commitTestingData.getTestLineCount())
                .codeLineCount(commitTestingData.getCodeLineCount())
                .unitTestCount(commitTestingData.getUnitTestCount())
                .integrationTestCount(commitTestingData.getIntegrationTestCount())
                .repositoryName(commitTestingData.getRepositoryName())
                .branchName(commitTestingData.getBranchName())
                .commitSha(commitTestingData.getCommitSha())
                .date(ZonedDateTime.ofInstant(commitTestingData.getDate().toInstant(),
                        ZoneId.systemDefault()))
                .build();
    }

    static CommitTestingDataView entityToDomain(final CommitTestingDataEntity commitTestingDataEntity) {
        return CommitTestingDataView.builder()
                .organizationId(commitTestingDataEntity.getOrganizationId())
                .coveredBranches(commitTestingDataEntity.getCoveredBranches())
                .totalBranchCount(commitTestingDataEntity.getTotalBranchCount())
                .testLineCount(commitTestingDataEntity.getTestLineCount())
                .codeLineCount(commitTestingDataEntity.getCodeLineCount())
                .unitTestCount(commitTestingDataEntity.getUnitTestCount())
                .integrationTestCount(commitTestingDataEntity.getIntegrationTestCount())
                .repositoryName(commitTestingDataEntity.getRepositoryName())
                .branchName(commitTestingDataEntity.getBranchName())
                .commitSha(commitTestingDataEntity.getCommitSha())
                .date(Date.from(commitTestingDataEntity.getDate().toInstant()))
                .build();
    }
}
