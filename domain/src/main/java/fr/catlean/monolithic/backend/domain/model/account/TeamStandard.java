package fr.catlean.monolithic.backend.domain.model.account;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TeamStandard {

    public static final String TIME_TO_MERGE = "time-to-merge";
    public static final String MAXIMUM_SIZE_TO_MERGE = "maximum-size-to-merge";
    String code;

    public static TeamStandard buildTimeToMerge() {
        return TeamStandard.builder()
                .code(TIME_TO_MERGE)
                .build();
    }

    public static TeamStandard buildMaximumSizeToMerge() {
        return TeamStandard.builder()
                .code(MAXIMUM_SIZE_TO_MERGE)
                .build();
    }
}
