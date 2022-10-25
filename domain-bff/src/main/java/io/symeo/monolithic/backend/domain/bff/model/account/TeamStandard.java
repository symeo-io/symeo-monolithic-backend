package io.symeo.monolithic.backend.domain.bff.model.account;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TeamStandard {

    public static final String TIME_TO_MERGE = "time-to-merge";
    public static final String PULL_REQUEST_SIZE = "pull-request-size";
    String code;

    public static TeamStandard buildTimeToMerge() {
        return TeamStandard.builder()
                .code(TIME_TO_MERGE)
                .build();
    }

    public static TeamStandard buildPullRequestSize() {
        return TeamStandard.builder()
                .code(PULL_REQUEST_SIZE)
                .build();
    }
}
