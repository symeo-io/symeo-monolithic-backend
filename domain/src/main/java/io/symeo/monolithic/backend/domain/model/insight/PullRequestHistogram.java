package io.symeo.monolithic.backend.domain.model.insight;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Value
@Builder
public class PullRequestHistogram {
    public static final String SIZE_LIMIT = "size-limit";
    public static final String TIME_LIMIT = "time-limit";
    String type;
    int limit;
    @NonNull
    UUID organizationId;
    @Builder.Default
    List<DataCompareToLimit> dataByWeek = new ArrayList<>();

    public void addDataBelowAndAboveLimitForWeek(int dataBelowLimit, int dataAboveLimit, String week) {
        this.dataByWeek.add(
                DataCompareToLimit.builder().numberBelowLimit(dataBelowLimit).numberAboveLimit(dataAboveLimit).dateAsString(week).build()
        );
    }
}
