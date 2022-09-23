package io.symeo.monolithic.backend.domain.model.insight;

import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Optional;

import static java.lang.Math.round;
import static java.util.Optional.empty;
import static java.util.Optional.of;

@Value
@Builder
public class AverageLeadTime {
    Float averageValue;
    Float averageCodingTime;
    Float averageReviewLag;
    Float averageReviewTime;
    Float averageDeployTime;

    public static Optional<AverageLeadTime> buildFromPullRequestWithCommitsViews(final List<PullRequestView> pullRequestWithCommitsViews) {
        final List<PullRequestView> pullRequestViewsToComputeLeadTime =
                filterPullRequestForLeadTimeComputation(pullRequestWithCommitsViews);
        final int pullRequestSize = pullRequestViewsToComputeLeadTime.size();
        if (pullRequestSize == 0) {
            return empty();
        }
        return of(computeLeadTime(pullRequestViewsToComputeLeadTime, pullRequestSize));
    }

    private static List<PullRequestView> filterPullRequestForLeadTimeComputation(List<PullRequestView> pullRequestWithCommitsViews) {
        return pullRequestWithCommitsViews.stream()
                .filter(AverageLeadTime::filterPullRequestToComputeLeadTime).toList();
    }

    private static AverageLeadTime computeLeadTime(List<PullRequestView> pullRequestViewsToComputeLeadTime,
                                                   int pullRequestSize) {
        Long cumulatedLeadTimeValue = 0L;
        Long cumulatedCodingTime = 0L;
        Long cumulatedReviewLag = 0L;
        Long cumulatedReviewTime = 0L;
        for (PullRequestView pullRequestWithCommitsView : pullRequestViewsToComputeLeadTime) {
            final LeadTime leadTime = LeadTime.computeLeadTimeForPullRequestView(pullRequestWithCommitsView);
            cumulatedCodingTime += leadTime.getCodingTime();
            cumulatedReviewLag += leadTime.getReviewLag();
            cumulatedReviewTime += leadTime.getReviewTime();
            cumulatedLeadTimeValue += leadTime.getValue();
        }
        return AverageLeadTime.builder()
                .averageValue(averageValueWithOneDecimal(cumulatedLeadTimeValue, pullRequestSize))
                .averageCodingTime(averageValueWithOneDecimal(cumulatedCodingTime, pullRequestSize))
                .averageReviewLag(averageValueWithOneDecimal(cumulatedReviewLag, pullRequestSize))
                .averageReviewTime(averageValueWithOneDecimal(cumulatedReviewTime, pullRequestSize))
                .build();
    }

    private static boolean filterPullRequestToComputeLeadTime(final PullRequestView pullRequestView) {
        return !pullRequestView.getCommitShaList().isEmpty() && pullRequestView.getStatus().equals(PullRequest.MERGE);
    }

    private static Float averageValueWithOneDecimal(Long cumulatedLeadTimeValue, int size) {
        return round(10 * cumulatedLeadTimeValue / size) / 10f;
    }


    public static Optional<AverageLeadTime> buildForPullRequestMergedOnBranchRegexSettings(final List<PullRequestView> pullRequestWithCommitsViews,
                                                                                           final List<PullRequestView> pullRequestViewsMergedOnMatchedBranches,
                                                                                           final List<Commit> allCommitsFromStartDate) {
        final List<PullRequestView> pullRequestViewsToComputeLeadTime =
                filterPullRequestForLeadTimeComputation(pullRequestWithCommitsViews);
        final int pullRequestSize = pullRequestViewsToComputeLeadTime.size();
        if (pullRequestSize == 0) {
            return empty();
        }
        return of(computeLeadTimeForPullRequestMergedOnBranchRegexSettings(pullRequestViewsToComputeLeadTime,
                pullRequestViewsMergedOnMatchedBranches, allCommitsFromStartDate, pullRequestSize));
    }

    private static AverageLeadTime computeLeadTimeForPullRequestMergedOnBranchRegexSettings(final List<PullRequestView> pullRequestViewsToComputeLeadTime,
                                                                                            final List<PullRequestView> pullRequestViewsMergedOnMatchedBranches,
                                                                                            final List<Commit> allCommitsFromStartDate,
                                                                                            int pullRequestSize) {
        Long cumulatedLeadTimeValue = 0L;
        Long cumulatedCodingTime = 0L;
        Long cumulatedReviewLag = 0L;
        Long cumulatedReviewTime = 0L;
        for (PullRequestView pullRequestWithCommitsView : pullRequestViewsToComputeLeadTime) {
            final LeadTime leadTime =
                    LeadTime.computeLeadTimeForMergeOnPullRequestMatchingDeliverySettings(pullRequestWithCommitsView,
                            pullRequestViewsMergedOnMatchedBranches, allCommitsFromStartDate);
            cumulatedCodingTime += leadTime.getCodingTime();
            cumulatedReviewLag += leadTime.getReviewLag();
            cumulatedReviewTime += leadTime.getReviewTime();
            cumulatedLeadTimeValue += leadTime.getValue();
        }
        return AverageLeadTime.builder()
                .averageValue(averageValueWithOneDecimal(cumulatedLeadTimeValue, pullRequestSize))
                .averageCodingTime(averageValueWithOneDecimal(cumulatedCodingTime, pullRequestSize))
                .averageReviewLag(averageValueWithOneDecimal(cumulatedReviewLag, pullRequestSize))
                .averageReviewTime(averageValueWithOneDecimal(cumulatedReviewTime, pullRequestSize))
                .build();
    }
}
