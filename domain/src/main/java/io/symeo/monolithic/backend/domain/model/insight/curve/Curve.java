package io.symeo.monolithic.backend.domain.model.insight.curve;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.round;

@Builder
public class Curve {

    @Builder.Default
    List<CurvePoint> data = new ArrayList<>();
    @Builder.Default
    private Map<String, List<Float>> valuesMappedToDate = new HashMap<>();

    public void addPoint(final String date, final Float value) {
        if (this.valuesMappedToDate.containsKey(date)) {
            this.valuesMappedToDate.get(date).add(value);
        } else {
            final ArrayList<Float> values = new ArrayList<>();
            values.add(value);
            this.valuesMappedToDate.put(date, values);
        }
    }

    public List<CurvePoint> getData() {
        this.valuesMappedToDate.forEach((date, values) -> this.data.add(
                CurvePoint.builder()
                        .date(date)
                        .value(round(10 * values.stream().reduce(0f, Float::sum) / values.size()) / 10f)
                        .build())
        );
        return this.data;
    }

    @Data
    @Builder
    public static class CurvePoint {
        String date;
        Float value;
    }
}
