package io.symeo.monolithic.backend.domain.model.insight;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class DataCompareToLimit {
    @Builder.Default
    int numberBelowLimit = 0;
    @Builder.Default
    int numberAboveLimit = 0;
    String dateAsString;

    public DataCompareToLimit incrementDataAboveLimit() {
        return this.toBuilder().numberAboveLimit(this.numberAboveLimit + 1).build();
    }

    public DataCompareToLimit incrementDataBelowLimit() {
        return this.toBuilder().numberBelowLimit(this.numberBelowLimit + 1).build();
    }
}
