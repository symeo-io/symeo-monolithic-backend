package io.symeo.monolithic.backend.domain.bff.model.metric;

import io.symeo.monolithic.backend.domain.bff.model.vcs.CommitView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.TagView;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class CycleTimePieceFactory {

    private final CycleTimeFactory cycleTimeFactory;

    public List<CycleTimePiece> computeForPullRequestMergedOnBranchRegexSettings(List<PullRequestView> pullRequestViewsToComputeCycleTime,
                                                                                 List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                                                                                 List<CommitView> allCommitsUntilEndDate) {
        List<CycleTimePiece> cycleTimePieces = new ArrayList<>();
        for (PullRequestView pullRequestView : pullRequestViewsToComputeCycleTime) {
            final CycleTime cycleTimeForPullRequestView =
                    cycleTimeFactory.computeCycleTimeForMergeOnPullRequestMatchingDeliverySettings(pullRequestView,
                            pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate, allCommitsUntilEndDate);

            cycleTimePieces.add(CycleTimePiece.builder()
                    .id(pullRequestView.getId())
                    .creationDate(pullRequestView.getCreationDate())
                    .mergeDate(pullRequestView.getMergeDate())
                    .state(pullRequestView.getStatus())
                    .vcsUrl(pullRequestView.getVcsUrl())
                    .title(pullRequestView.getTitle())
                    .author(pullRequestView.getAuthorLogin())
                    .repository(pullRequestView.getRepository())
                    .cycleTime(cycleTimeForPullRequestView.getValue())
                    .codingTime(cycleTimeForPullRequestView.getCodingTime())
                    .reviewTime(cycleTimeForPullRequestView.getReviewTime())
                    .timeToDeploy(cycleTimeForPullRequestView.getTimeToDeploy())
                    .build());
        }
        return cycleTimePieces;
    }

    public List<CycleTimePiece> computeForTagRegexSettings(List<PullRequestView> pullRequestViewsToComputeCycleTime,
                                                           List<TagView> tagsMatchingDeployTagRegex,
                                                           List<CommitView> allCommitsUntilEndDate) {
        List<CycleTimePiece> cycleTimePieces = new ArrayList<>();
        for (PullRequestView pullRequestView : pullRequestViewsToComputeCycleTime) {
            final CycleTime cycleTimeForTagRegexSettings =
                    cycleTimeFactory.computeCycleTimeForTagRegexToDeploySettings(pullRequestView,
                            tagsMatchingDeployTagRegex, allCommitsUntilEndDate);

            cycleTimePieces.add(CycleTimePiece.builder()
                    .id(pullRequestView.getId())
                    .creationDate(pullRequestView.getCreationDate())
                    .mergeDate(pullRequestView.getMergeDate())
                    .state(pullRequestView.getStatus())
                    .vcsUrl(pullRequestView.getVcsUrl())
                    .title(pullRequestView.getTitle())
                    .author(pullRequestView.getAuthorLogin())
                    .repository(pullRequestView.getRepository())
                    .cycleTime(cycleTimeForTagRegexSettings.getValue())
                    .codingTime(cycleTimeForTagRegexSettings.getCodingTime())
                    .reviewTime(cycleTimeForTagRegexSettings.getReviewTime())
                    .timeToDeploy(cycleTimeForTagRegexSettings.getTimeToDeploy())
                    .build());
        }
        return cycleTimePieces;
    }
}
