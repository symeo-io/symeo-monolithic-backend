package io.symeo.monolithic.backend.domain.helper;

import static java.lang.Math.round;
import static java.util.Objects.isNull;

public class MetricsHelper {

    public static Float getTendencyPercentage(final Float currentValue, final Float previousValue) {
        if (isNull(currentValue) || isNull(previousValue)) {
            return null;
        }
        return round(previousValue == 0 ? 0 : 1000 * (currentValue - previousValue) / previousValue) / 10f;
    }

    public static Float getTendencyPercentage(final Integer currentValue, final Integer previousValue) {
        return MetricsHelper.getTendencyPercentage((float) currentValue, (float) previousValue);
    }
}
