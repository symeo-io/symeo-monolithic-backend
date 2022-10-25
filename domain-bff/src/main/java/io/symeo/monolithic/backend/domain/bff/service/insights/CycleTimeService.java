package io.symeo.monolithic.backend.domain.bff.service.insights;

import io.symeo.monolithic.backend.domain.bff.model.metric.AverageCycleTime;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTimePiece;
import io.symeo.monolithic.backend.domain.bff.model.vcs.CommitView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.TagView;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public class CycleTimeService {

    public Optional<AverageCycleTime> buildForTagRegexSettings(final List<PullRequestView> pullRequestViews,
                                                               final List<TagView> tagsMatchingDeployTagRegex,
                                                               final List<CommitView> allCommitsUntilEndDate) {
        final List<PullRequestView> pullRequestViewsToComputeCycleTime =
                filterPullRequestForCycleTimeComputation(pullRequestViews);
        if (pullRequestViewsToComputeCycleTime.size() == 0) {
            return empty();
        }
        return of(AverageCycleTime.computeCycleTimeForTagRegexToDeploySettings(pullRequestViewsToComputeCycleTime,
                tagsMatchingDeployTagRegex, allCommitsUntilEndDate));
    }

    public Optional<AverageCycleTime> buildForPullRequestMergedOnBranchRegexSettings(final List<PullRequestView> pullRequestViews,
                                                                                     final List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                                                                                     final List<CommitView> allCommitsUntilEndDate) {
        final List<PullRequestView> pullRequestViewsToComputeCycleTime =
                filterPullRequestForCycleTimeComputation(pullRequestViews);
        if (pullRequestViewsToComputeCycleTime.size() == 0) {
            return empty();
        }
        return of(AverageCycleTime.computeCycleTimeForPullRequestMergedOnBranchRegexSettings(pullRequestViewsToComputeCycleTime,
                pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate, allCommitsUntilEndDate));
    }

    public List<CycleTimePiece> buildCycleTimePiecesForPullRequestsMergedOnBranchRegexSettings(List<PullRequestView> pullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted,
                                                                                               List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                                                                                               List<CommitView> allCommitsUntilEndDate) {
        final List<PullRequestView> pullRequestViewsToComputeCycleTime =
                filterPullRequestForCycleTimeComputation(pullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted);
        if (pullRequestViewsToComputeCycleTime.size() == 0) {
            return List.of();
        }
        return CycleTimePiece.computeForPullRequestMergedOnBranchRegexSettings(pullRequestViewsToComputeCycleTime,
                pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate, allCommitsUntilEndDate);
    }

    public List<CycleTimePiece> buildCycleTimePiecesForTagRegexSettings(List<PullRequestView> pullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted,
                                                                        List<TagView> tagsMatchingDeployTagRegex,
                                                                        List<CommitView> allCommitsUntilEndDate) {
        final List<PullRequestView> pullRequestViewsToComputeCycleTime =
                filterPullRequestForCycleTimeComputation(pullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted);
        if (pullRequestViewsToComputeCycleTime.size() == 0) {
            return List.of();
        }
        return CycleTimePiece.computeForTagRegexSettings(pullRequestViewsToComputeCycleTime,
                tagsMatchingDeployTagRegex, allCommitsUntilEndDate);
    }

    private static List<PullRequestView> filterPullRequestForCycleTimeComputation(List<PullRequestView> pullRequestWithCommitsViews) {
        return pullRequestWithCommitsViews.stream()
                .filter(CycleTimeService::filterPullRequestToComputeCycleTime).toList();
    }

    public static boolean filterPullRequestToComputeCycleTime(final PullRequestView pullRequestView) {
        return nonNull(pullRequestView.getCommitShaList()) && !pullRequestView.getCommitShaList().isEmpty();
    }
}