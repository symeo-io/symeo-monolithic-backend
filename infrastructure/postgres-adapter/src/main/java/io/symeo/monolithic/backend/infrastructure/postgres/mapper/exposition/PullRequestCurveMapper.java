package io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition;

import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.dto.PullRequestSizeDTO;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.dto.PullRequestTimeToMergeDTO;

import java.util.Date;

import static java.util.Objects.isNull;

public interface PullRequestCurveMapper {

    static PullRequestView dtoToView(final PullRequestTimeToMergeDTO pullRequestTimeToMergeDTO) {
        return PullRequestView.builder()
                .creationDate(Date.from(pullRequestTimeToMergeDTO.getCreationDate().toInstant()))
                .closeDate(isNull(pullRequestTimeToMergeDTO.getCloseDate()) ? null :
                        Date.from(pullRequestTimeToMergeDTO.getCloseDate().toInstant()))
                .mergeDate(isNull(pullRequestTimeToMergeDTO.getMergeDate()) ? null :
                        Date.from(pullRequestTimeToMergeDTO.getMergeDate().toInstant()))
                .status(pullRequestTimeToMergeDTO.getState())
                .vcsUrl(pullRequestTimeToMergeDTO.getVcsUrl())
                .head(pullRequestTimeToMergeDTO.getHead())
                .build();
    }

    static PullRequestView dtoToView(final PullRequestSizeDTO pullRequestSizeDTO) {
        return PullRequestView.builder()
                .creationDate(Date.from(pullRequestSizeDTO.getCreationDate().toInstant()))
                .closeDate(isNull(pullRequestSizeDTO.getCloseDate()) ? null :
                        Date.from(pullRequestSizeDTO.getCloseDate().toInstant()))
                .mergeDate(isNull(pullRequestSizeDTO.getMergeDate()) ? null :
                        Date.from(pullRequestSizeDTO.getMergeDate().toInstant()))
                .status(pullRequestSizeDTO.getState())
                .deletedLineNumber(pullRequestSizeDTO.getDeletedLineNumber())
                .addedLineNumber(pullRequestSizeDTO.getAddedLineNumber())
                .vcsUrl(pullRequestSizeDTO.getVcsUrl())
                .head(pullRequestSizeDTO.getHead())
                .build();
    }
}
