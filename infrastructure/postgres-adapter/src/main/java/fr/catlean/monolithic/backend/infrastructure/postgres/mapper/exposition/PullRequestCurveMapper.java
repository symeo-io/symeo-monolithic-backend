package fr.catlean.monolithic.backend.infrastructure.postgres.mapper.exposition;

import fr.catlean.monolithic.backend.domain.model.insight.view.PullRequestTimeToMergeView;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.dto.PullRequestTimeToMergeDTO;

public interface PullRequestCurveMapper {

    static PullRequestTimeToMergeView dtoToView(final PullRequestTimeToMergeDTO pullRequestTimeToMergeDTO) {
        return PullRequestTimeToMergeView.builder()
                .status(pullRequestTimeToMergeDTO.getState())
                .daysOpen(pullRequestTimeToMergeDTO.getDaysOpened())
                .startDateRange(pullRequestTimeToMergeDTO.getStartDateRange())
                .build();
    }
}
