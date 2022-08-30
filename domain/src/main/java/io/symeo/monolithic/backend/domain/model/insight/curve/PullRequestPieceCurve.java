package io.symeo.monolithic.backend.domain.model.insight.curve;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class PullRequestPieceCurve {

    @Builder.Default
    List<PullRequestPieceCurvePoint> data = new ArrayList<>();

    public void addPoint(final String startDateRange, final Float value, final boolean open, final String label,
                         final String link) {
        this.data.add(
                PullRequestPieceCurvePoint.builder()
                        .date(startDateRange)
                        .value(value)
                        .open(open)
                        .label(label)
                        .link(link)
                        .build()
        );
    }

    @Data
    @Builder
    public static class PullRequestPieceCurvePoint {
        String date;
        Float value;
        Boolean open;
        String label;
        String link;
    }
}
