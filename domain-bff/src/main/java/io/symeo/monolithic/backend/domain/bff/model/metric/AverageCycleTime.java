package io.symeo.monolithic.backend.domain.bff.model.metric;

import io.symeo.monolithic.backend.domain.bff.model.vcs.CommitView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.TagView;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.function.Function;

import static java.lang.Math.round;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Value
@Builder
public class AverageCycleTime {
    Float averageValue;
    Float averageCodingTime;
    Float averageReviewTime;
    Float averageDeployTime;

    public static AverageCycleTime computeCycleTimeForPullRequestMergedOnBranchRegexSettings(final List<PullRequestView> pullRequestViewsToComputeCycleTime,
                                                                                             final List<PullRequestView> pullRequestViewsMergedOnMatchedBranches,
                                                                                             final List<CommitView> allCommits) {
        return computeCycleTimeFromFunction(pullRequestViewsToComputeCycleTime,
                cycleTimeForTimeToMergeOnPullRequestMatchingDeliverySettings(pullRequestViewsMergedOnMatchedBranches,
                        allCommits));
    }

    public static AverageCycleTime computeCycleTimeForTagRegexToDeploySettings(final List<PullRequestView> pullRequestViewsToComputeCycleTime,
                                                                               final List<TagView> tagsMatchedToDeploy,
                                                                               final List<CommitView> allCommits) {
        return computeCycleTimeFromFunction(pullRequestViewsToComputeCycleTime,
                cycleTimeForTagRegexToDeploySettings(tagsMatchedToDeploy, allCommits));
    }

    private static AverageCycleTime computeCycleTimeFromFunction(
            final List<PullRequestView> pullRequestViewsToComputeCycleTime,
            final Function<PullRequestView, CycleTime> pullRequestViewToCycleTimeFunction) {
        Long cumulatedCycleTimeValue = null;
        Long cumulatedCodingTime = null;
        Long cumulatedReviewTime = null;
        Long cumulatedDeployTime = null;
        int numberOfCycleTime = 0;
        int numberOfCodingTime = 0;
        int numberOfReviewTime = 0;
        int numberOfDeployTime = 0;
        for (PullRequestView pullRequestWithCommitsView : pullRequestViewsToComputeCycleTime) {
            final CycleTime cycleTime = pullRequestViewToCycleTimeFunction.apply(pullRequestWithCommitsView);
            if (nonNull(cycleTime.getCodingTime())) {
                if (isNull(cumulatedCodingTime)) {
                    cumulatedCodingTime = cycleTime.getCodingTime();
                } else {
                    cumulatedCodingTime += cycleTime.getCodingTime();
                }
                numberOfCodingTime++;
            }
            if (nonNull(cycleTime.getReviewTime())) {
                if (isNull(cumulatedReviewTime)) {
                    cumulatedReviewTime = cycleTime.getReviewTime();
                } else {
                    cumulatedReviewTime += cycleTime.getReviewTime();
                }
                numberOfReviewTime++;
            }
            if (nonNull(cycleTime.getDeployTime())) {
                if (isNull(cumulatedDeployTime)) {
                    cumulatedDeployTime = cycleTime.getDeployTime();
                } else {
                    cumulatedDeployTime += cycleTime.getDeployTime();
                }
                numberOfDeployTime++;
            }
            if (nonNull(cycleTime.getValue())) {
                if (isNull(cumulatedCycleTimeValue)) {
                    cumulatedCycleTimeValue = cycleTime.getValue();
                } else {
                    cumulatedCycleTimeValue += cycleTime.getValue();
                }
                numberOfCycleTime++;
            }
        }
        return AverageCycleTime.builder()
                .averageValue(averageValueWithOneDecimal(cumulatedCycleTimeValue, numberOfCycleTime))
                .averageCodingTime(averageValueWithOneDecimal(cumulatedCodingTime, numberOfCodingTime))
                .averageReviewTime(averageValueWithOneDecimal(cumulatedReviewTime, numberOfReviewTime))
                .averageDeployTime(averageValueWithOneDecimal(cumulatedDeployTime, numberOfDeployTime))
                .build();
    }

    private static Function<PullRequestView, CycleTime> cycleTimeForTimeToMergeOnPullRequestMatchingDeliverySettings(
            final List<PullRequestView> pullRequestViewsMergedOnMatchedBranches,
            final List<CommitView> allCommits) {
        return pullRequestView -> CycleTime.computeCycleTimeForMergeOnPullRequestMatchingDeliverySettings(pullRequestView,
                pullRequestViewsMergedOnMatchedBranches, allCommits);
    }

    private static Function<PullRequestView, CycleTime> cycleTimeForTagRegexToDeploySettings(
            final List<TagView> tagsMatchedToDeploy,
            final List<CommitView> allCommits) {
        return pullRequestView -> CycleTime.computeCycleTimeForTagRegexToDeploySettings(pullRequestView,
                tagsMatchedToDeploy, allCommits);
    }

    private static Float averageValueWithOneDecimal(Long cumulatedValue, int size) {
        return isNull(cumulatedValue) ? null : round(10f * cumulatedValue / size) / 10f;
    }


}
