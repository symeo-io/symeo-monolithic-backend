package io.symeo.monolithic.backend.domain.service.insights;

import io.symeo.monolithic.backend.domain.model.insight.AverageCycleTime;
import io.symeo.monolithic.backend.domain.model.insight.CycleTimePiece;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Tag;

import java.util.List;
import java.util.Optional;

import static io.symeo.monolithic.backend.domain.model.insight.AverageCycleTime.computeCycleTimeForPullRequestMergedOnBranchRegexSettings;
import static io.symeo.monolithic.backend.domain.model.insight.AverageCycleTime.computeCycleTimeForTagRegexToDeploySettings;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public class CycleTimeService {

    public Optional<AverageCycleTime> buildForTagRegexSettings(final List<PullRequestView> pullRequestViews,
                                                               final List<Tag> tagsMatchingDeployTagRegex,
                                                               final List<Commit> allCommitsUntilEndDate) {
        final List<PullRequestView> pullRequestViewsToComputeCycleTime =
                filterPullRequestForCycleTimeComputation(pullRequestViews);
        if (pullRequestViewsToComputeCycleTime.size() == 0) {
            return empty();
        }
        return of(computeCycleTimeForTagRegexToDeploySettings(pullRequestViewsToComputeCycleTime,
                tagsMatchingDeployTagRegex, allCommitsUntilEndDate));
    }

    public Optional<AverageCycleTime> buildForPullRequestMergedOnBranchRegexSettings(final List<PullRequestView> pullRequestViews,
                                                                                     final List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                                                                                     final List<Commit> allCommitsUntilEndDate) {
        final List<PullRequestView> pullRequestViewsToComputeCycleTime =
                filterPullRequestForCycleTimeComputation(pullRequestViews);
        if (pullRequestViewsToComputeCycleTime.size() == 0) {
            return empty();
        }
        return of(computeCycleTimeForPullRequestMergedOnBranchRegexSettings(pullRequestViewsToComputeCycleTime,
                pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate, allCommitsUntilEndDate));
    }

    public List<CycleTimePiece> buildCycleTimePiecesForPullRequestsMergedOnBranchRegexSettings(List<PullRequestView> pullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted,
                                                                                               List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                                                                                               List<Commit> allCommitsUntilEndDate) {
        final List<PullRequestView> pullRequestViewsToComputeCycleTime =
                filterPullRequestForCycleTimeComputation(pullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted);
        if (pullRequestViewsToComputeCycleTime.size() == 0) {
            return List.of();
        }
        return CycleTimePiece.computeForPullRequestMergedOnBranchRegexSettings(pullRequestViewsToComputeCycleTime,
                pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate, allCommitsUntilEndDate);
    }

    public List<CycleTimePiece> buildCycleTimePiecesForTagRegexSettings(List<PullRequestView> pullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted,
                                                                        List<Tag> tagsMatchingDeployTagRegex,
                                                                        List<Commit> allCommitsUntilEndDate) {
        final List<PullRequestView> pullRequestViewsToComputeCycleTime =
                filterPullRequestForCycleTimeComputation(pullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted);
        if (pullRequestViewsToComputeCycleTime.size() == 0) {
            return List.of();
        }
        return CycleTimePiece.computeForTagRegexSettings(pullRequestViewsToComputeCycleTime,
                tagsMatchingDeployTagRegex,allCommitsUntilEndDate);
    }

    private static List<PullRequestView> filterPullRequestForCycleTimeComputation(List<PullRequestView> pullRequestWithCommitsViews) {
        return pullRequestWithCommitsViews.stream()
                .filter(CycleTimeService::filterPullRequestToComputeCycleTime).toList();
    }

    public static boolean filterPullRequestToComputeCycleTime(final PullRequestView pullRequestView) {
        return nonNull(pullRequestView.getCommitShaList()) && !pullRequestView.getCommitShaList().isEmpty();
    }
}
