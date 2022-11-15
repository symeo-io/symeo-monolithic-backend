package io.symeo.monolithic.backend.job.domain.model.vcs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Data
@Slf4j
public class CycleTime {
    String id;
    Long value;
    Long codingTime;
    Long reviewTime;
    Long timeToDeploy;
    Date deployDate;
    // To display PR data on graphs
    PullRequest pullRequest;
}
