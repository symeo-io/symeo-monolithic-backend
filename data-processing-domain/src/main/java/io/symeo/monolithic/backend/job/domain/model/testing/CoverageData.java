package io.symeo.monolithic.backend.job.domain.model.testing;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class CoverageData {
    Integer totalBranchCount;
    Integer coveredBranches;
}
