package io.symeo.monolithic.backend.domain.bff.model.metric;

import lombok.Builder;
import lombok.Value;

import java.util.Date;
import java.util.UUID;

@Builder
@Value
public class CommitTestingDataView {
    UUID organizationId;
    Integer totalBranchCount;
    Integer coveredBranches;
    Integer codeLineCount;
    Integer testLineCount;
    Integer unitTestCount;
    Integer integrationTestCount;
    String repositoryName;
    String branchName;
    String commitSha;
    Date date;
}
