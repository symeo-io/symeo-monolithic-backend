package io.symeo.monolithic.backend.domain.model.insight;

import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Tag;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Objects;

import static java.lang.Math.round;

@Value
@Builder
public class AverageCycleTime {
    Float averageValue;
    Float averageCodingTime;
    Float averageReviewTime;
    Float averageDeployTime;


    private static Float averageValueWithOneDecimal(Long cumulatedCycleTimeValue, int size) {
        return round(10f * cumulatedCycleTimeValue / size) / 10f;
    }

    public static AverageCycleTime computeCycleTimeForPullRequestMergedOnBranchRegexSettings(final List<PullRequestView> pullRequestViewsToComputeCycleTime,
                                                                                             final List<PullRequestView> pullRequestViewsMergedOnMatchedBranches,
                                                                                             final List<Commit> allCommitsFromStartDate,
                                                                                             int pullRequestSize) {
        Long cumulatedCycleTimeValue = 0L;
        Long cumulatedCodingTime = 0L;
        Long cumulatedReviewTime = 0L;
        Long cumulatedDeployTime = 0L;
        for (PullRequestView pullRequestWithCommitsView : pullRequestViewsToComputeCycleTime) {
            final CycleTime cycleTime =
                    CycleTime.computeCycleTimeForMergeOnPullRequestMatchingDeliverySettings(pullRequestWithCommitsView,
                            pullRequestViewsMergedOnMatchedBranches, allCommitsFromStartDate);
            cumulatedCodingTime += cycleTime.getCodingTime();
            cumulatedReviewTime += cycleTime.getReviewTime();
            cumulatedCycleTimeValue += cycleTime.getValue();
            cumulatedDeployTime += Objects.isNull(cycleTime.getDeployTime()) ? 0L : cycleTime.getDeployTime();
        }
        return AverageCycleTime.builder()
                .averageValue(averageValueWithOneDecimal(cumulatedCycleTimeValue, pullRequestSize))
                .averageCodingTime(averageValueWithOneDecimal(cumulatedCodingTime, pullRequestSize))
                .averageReviewTime(averageValueWithOneDecimal(cumulatedReviewTime, pullRequestSize))
                .averageDeployTime(averageValueWithOneDecimal(cumulatedDeployTime, pullRequestSize))
                .build();
    }

    public static AverageCycleTime computeCycleTimeForTagRegexToDeploySettings(final List<PullRequestView> pullRequestViewsToComputeCycleTime,
                                                                               final List<Tag> tagsMatchedToDeploy,
                                                                               final List<Commit> allCommitsFromStartDate,
                                                                               int pullRequestSize) {
        Long cumulatedCycleTimeValue = 0L;
        Long cumulatedCodingTime = 0L;
        Long cumulatedReviewTime = 0L;
        Long cumulatedDeployTime = 0L;
        for (PullRequestView pullRequestWithCommitsView : pullRequestViewsToComputeCycleTime) {
            final CycleTime cycleTime =
                    CycleTime.computeCycleTimeForTagRegexToDeploySettings(pullRequestWithCommitsView,
                            tagsMatchedToDeploy, allCommitsFromStartDate);
            cumulatedCodingTime += cycleTime.getCodingTime();
            cumulatedReviewTime += cycleTime.getReviewTime();
            cumulatedCycleTimeValue += cycleTime.getValue();
            cumulatedDeployTime += Objects.isNull(cycleTime.getDeployTime()) ? 0L : cycleTime.getDeployTime();
        }
        return AverageCycleTime.builder()
                .averageValue(averageValueWithOneDecimal(cumulatedCycleTimeValue, pullRequestSize))
                .averageCodingTime(averageValueWithOneDecimal(cumulatedCodingTime, pullRequestSize))
                .averageReviewTime(averageValueWithOneDecimal(cumulatedReviewTime, pullRequestSize))
                .averageDeployTime(averageValueWithOneDecimal(cumulatedDeployTime, pullRequestSize))
                .build();
    }


}
