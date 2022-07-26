package fr.catlean.monolithic.backend.domain.model.insight.curve;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder
public class Curve {

    @Builder.Default
    List<CurvePoint> data = new ArrayList<>();
    @Builder.Default
    private Map<String, List<Integer>> valuesMappedToDate = new HashMap<>();

    public void addPoint(String date, Integer value) {
        if (this.valuesMappedToDate.containsKey(date)) {
            this.valuesMappedToDate.get(date).add(value);
        } else {
            final ArrayList<Integer> values = new ArrayList<>();
            values.add(value);
            this.valuesMappedToDate.put(date, values);
        }
    }

    public List<CurvePoint> getData() {
        this.valuesMappedToDate.forEach((date, values) -> this.data.add(
                CurvePoint.builder()
                        .date(date)
                        .value(values.stream().reduce(0, Integer::sum) / values.size())
                        .build())
        );
        return this.data;
    }

    @Data
    @Builder
    public static class CurvePoint {
        String date;
        Integer value;
    }
}
