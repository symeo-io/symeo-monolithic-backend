package io.symeo.monolithic.backend.job.domain.model.testing;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Builder
@Value
public class CommitTestingData {
    UUID organizationId;
    CoverageData coverage;
    Integer codeLineCount;
    Integer testLineCount;
    Integer unitTestCount;
    Integer integrationTestCount;
    String repositoryName;
    String branchName;
    String commitSha;
}
