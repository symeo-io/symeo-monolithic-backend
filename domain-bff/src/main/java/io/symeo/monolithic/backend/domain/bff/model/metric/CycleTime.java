package io.symeo.monolithic.backend.domain.bff.model.metric;

import io.symeo.monolithic.backend.domain.bff.model.vcs.*;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Builder(toBuilder = true)
@Data
@Slf4j
public class CycleTime {

    Long value;
    Long codingTime;
    Long reviewTime;
    Long deployTime;
    // To display PR data on graphs
    PullRequestView pullRequestView;
}
