package io.symeo.monolithic.backend.domain.bff.model.metric.curve;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class CycleTimePieceCurve {
    @Builder.Default
    List<CyclePieceCurvePoint> data = new ArrayList<>();

    public void addPoint(final String startDateRange, final Long value, final Long codingTime,
                         final Long reviewTime, final Long deployTime, final String link, final String label) {
        this.data.add(
                CyclePieceCurvePoint.builder()
                        .date(startDateRange)
                        .value(value)
                        .codingTime(codingTime)
                        .reviewTime(reviewTime)
                        .deployTime(deployTime)
                        .link(link)
                        .label(label)
                        .build()
        );
    }

    @Data
    @Builder
    public static class CyclePieceCurvePoint {
        String date;
        Long value;
        Long codingTime;
        Long reviewTime;
        Long deployTime;
        String label;
        String link;
    }
}
