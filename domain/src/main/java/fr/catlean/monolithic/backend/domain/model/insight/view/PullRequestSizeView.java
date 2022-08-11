package fr.catlean.monolithic.backend.domain.model.insight.view;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PullRequestSizeView {
    Integer size;
    String startDateRange;
    String status;
}
