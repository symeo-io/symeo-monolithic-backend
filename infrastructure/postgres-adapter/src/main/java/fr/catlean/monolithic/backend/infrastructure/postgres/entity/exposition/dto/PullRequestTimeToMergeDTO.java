package fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PullRequestTimeToMergeDTO {

    int daysOpened;
    String startDateRange;
    String state;
}
