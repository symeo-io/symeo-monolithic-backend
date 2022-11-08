package io.symeo.monolithic.backend.job.domain.model.job.runnable.task;

import io.symeo.monolithic.backend.job.domain.model.vcs.Repository;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;

import java.util.Date;
import java.util.List;

@Builder
@Value
@ToString
public class RepositoryDateRangeTask {
    Repository repository;
    Date startDate;
    Date endDate;
    String deployDetectionType;
    String pullRequestMergedOnBranchRegex;
    String tagRegex;
    List<String> excludedBranchRegexes;
}
