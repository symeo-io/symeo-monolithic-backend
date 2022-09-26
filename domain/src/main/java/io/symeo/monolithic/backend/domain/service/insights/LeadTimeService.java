package io.symeo.monolithic.backend.domain.service.insights;

import io.symeo.monolithic.backend.domain.model.insight.AverageLeadTime;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Tag;

import java.util.List;
import java.util.Optional;

import static io.symeo.monolithic.backend.domain.model.insight.AverageLeadTime.computeLeadTimeForPullRequestMergedOnBranchRegexSettings;
import static io.symeo.monolithic.backend.domain.model.insight.AverageLeadTime.computeLeadTimeForTagRegexToDeploySettings;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public class LeadTimeService {

    public Optional<AverageLeadTime> buildForTagRegexSettings(final List<PullRequestView> pullRequestViews,
                                                              final List<Tag> tagsMatchingDeployTagRegex,
                                                              final List<Commit> allCommitsUntilEndDate) {
        final List<PullRequestView> pullRequestViewsToComputeLeadTime =
                filterPullRequestForLeadTimeComputation(pullRequestViews);
        final int pullRequestSize = pullRequestViewsToComputeLeadTime.size();
        if (pullRequestSize == 0) {
            return empty();
        }
        return of(computeLeadTimeForTagRegexToDeploySettings(pullRequestViewsToComputeLeadTime,
                tagsMatchingDeployTagRegex, allCommitsUntilEndDate, pullRequestSize));
    }

    public Optional<AverageLeadTime> buildForPullRequestMergedOnBranchRegexSettings(final List<PullRequestView> pullRequestViews,
                                                                                    final List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                                                                                    final List<Commit> allCommitsUntilEndDate) {
        final List<PullRequestView> pullRequestViewsToComputeLeadTime =
                filterPullRequestForLeadTimeComputation(pullRequestViews);
        final int pullRequestSize = pullRequestViewsToComputeLeadTime.size();
        if (pullRequestSize == 0) {
            return empty();
        }
        return of(computeLeadTimeForPullRequestMergedOnBranchRegexSettings(pullRequestViewsToComputeLeadTime,
                pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate, allCommitsUntilEndDate,
                pullRequestSize));
    }

    private static List<PullRequestView> filterPullRequestForLeadTimeComputation(List<PullRequestView> pullRequestWithCommitsViews) {
        return pullRequestWithCommitsViews.stream()
                .filter(LeadTimeService::filterPullRequestToComputeLeadTime).toList();
    }

    public static boolean filterPullRequestToComputeLeadTime(final PullRequestView pullRequestView) {
        return !pullRequestView.getCommitShaList().isEmpty() && pullRequestView.getStatus().equals(PullRequest.MERGE);
    }
}
