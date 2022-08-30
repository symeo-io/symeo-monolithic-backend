package io.symeo.monolithic.backend.domain.model.insight.curve;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class LeadTimePieceCurve {
    @Builder.Default
    List<LeadTimePieceCurvePoint> data = new ArrayList<>();

    public void addPoint(final String startDateRange, final Long value, final Long codingTime,
                         final Long reviewLag, final Long reviewTime, final String link, final String label) {
        this.data.add(
                LeadTimePieceCurvePoint.builder()
                        .date(startDateRange)
                        .value(value)
                        .codingTime(codingTime)
                        .reviewLag(reviewLag)
                        .reviewTime(reviewTime)
                        .link(link)
                        .label(label)
                        .build()
        );
    }

    @Data
    @Builder
    public static class LeadTimePieceCurvePoint {
        String date;
        Long value;
        Long codingTime;
        Long reviewLag;
        Long reviewTime;
        String label;
        String link;
    }
}
