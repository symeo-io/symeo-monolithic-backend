package fr.catlean.monolithic.backend.domain.model.insight.view;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PullRequestTimeToMergeView {
    Integer daysOpen;
    String startDateRange;
    String status;
}
