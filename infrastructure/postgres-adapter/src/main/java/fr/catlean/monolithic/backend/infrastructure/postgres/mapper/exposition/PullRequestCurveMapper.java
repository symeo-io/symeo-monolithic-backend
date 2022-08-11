package fr.catlean.monolithic.backend.infrastructure.postgres.mapper.exposition;

import fr.catlean.monolithic.backend.domain.model.insight.view.PullRequestSizeView;
import fr.catlean.monolithic.backend.domain.model.insight.view.PullRequestTimeToMergeView;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.dto.PullRequestSizeDTO;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.dto.PullRequestTimeToMergeDTO;

public interface PullRequestCurveMapper {

    static PullRequestTimeToMergeView dtoToView(final PullRequestTimeToMergeDTO pullRequestTimeToMergeDTO) {
        return PullRequestTimeToMergeView.builder()
                .status(pullRequestTimeToMergeDTO.getState())
                .daysOpen(pullRequestTimeToMergeDTO.getDaysOpened())
                .startDateRange(pullRequestTimeToMergeDTO.getStartDateRange())
                .build();
    }

    static PullRequestSizeView dtoToView(final PullRequestSizeDTO pullRequestSizeDTO) {
        return PullRequestSizeView.builder()
                .status(pullRequestSizeDTO.getState())
                .startDateRange(pullRequestSizeDTO.getStartDateRange())
                .size(pullRequestSizeDTO.getSize())
                .build();
    }
}
