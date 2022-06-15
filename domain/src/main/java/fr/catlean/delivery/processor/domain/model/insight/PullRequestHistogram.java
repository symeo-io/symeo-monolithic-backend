package fr.catlean.delivery.processor.domain.model.insight;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
@Builder
public class PullRequestHistogram {
    public static final String SIZE_LIMIT = "size-limit";
    public static final String TIME_LIMIT = "time-limit";
    String type;
    int limit;
    @NonNull
    String organizationAccount;
    @Builder.Default
    String team = "all";
    @Builder.Default
    List<DataCompareToLimit> dataByWeek = new ArrayList<>();

    public void addDataBelowAndAboveLimitForWeek(int dataBelowLimit, int dataAboveLimit, String week) {
        this.dataByWeek.add(
                DataCompareToLimit.builder().numberBelowLimit(dataBelowLimit).numberAboveLimit(dataAboveLimit).dateAsString(week).build()
        );
    }
}
