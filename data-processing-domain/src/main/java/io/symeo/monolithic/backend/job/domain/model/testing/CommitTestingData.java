package io.symeo.monolithic.backend.job.domain.model.testing;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class CommitTestingData {
    Float coverage;
    Integer codeLineCount;
    Integer testLineCount;
    Integer testCount;
    String testType;
    String repositoryName;
    String branchName;
    String commitSha;
}
