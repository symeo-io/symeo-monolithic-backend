package fr.catlean.monolithic.backend.domain.model.insight.curve;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class PieceCurve {

    @Builder.Default
    List<PieceCurvePoint> data = new ArrayList<>();

    public void addPoint(String startDateRange, Integer daysOpen, boolean open) {
        this.data.add(
                PieceCurvePoint.builder()
                        .date(startDateRange)
                        .value(daysOpen)
                        .open(open)
                        .build()
        );
    }

    @Data
    @Builder
    public static class PieceCurvePoint {
        String date;
        Integer value;
        Boolean open;
    }
}
