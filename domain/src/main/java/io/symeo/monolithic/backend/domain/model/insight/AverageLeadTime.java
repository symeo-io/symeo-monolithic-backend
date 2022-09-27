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
public class AverageLeadTime {
    Float averageValue;
    Float averageCodingTime;
    Float averageReviewLag;
    Float averageReviewTime;
    Float averageDeployTime;


    private static Float averageValueWithOneDecimal(Long cumulatedLeadTimeValue, int size) {
        return round(10f * cumulatedLeadTimeValue / size) / 10f;
    }

    public static AverageLeadTime computeLeadTimeForPullRequestMergedOnBranchRegexSettings(final List<PullRequestView> pullRequestViewsToComputeLeadTime,
                                                                                           final List<PullRequestView> pullRequestViewsMergedOnMatchedBranches,
                                                                                           final List<Commit> allCommitsFromStartDate,
                                                                                           int pullRequestSize) {
        Long cumulatedLeadTimeValue = 0L;
        Long cumulatedCodingTime = 0L;
        Long cumulatedReviewLag = 0L;
        Long cumulatedReviewTime = 0L;
        Long cumulatedDeployTime = 0L;
        for (PullRequestView pullRequestWithCommitsView : pullRequestViewsToComputeLeadTime) {
            final CycleTime cycleTime =
                    CycleTime.computeLeadTimeForMergeOnPullRequestMatchingDeliverySettings(pullRequestWithCommitsView,
                            pullRequestViewsMergedOnMatchedBranches, allCommitsFromStartDate);
            cumulatedCodingTime += cycleTime.getCodingTime();
            cumulatedReviewLag += cycleTime.getReviewLag();
            cumulatedReviewTime += cycleTime.getReviewTime();
            cumulatedLeadTimeValue += cycleTime.getValue();
            cumulatedDeployTime += Objects.isNull(cycleTime.getDeployTime()) ? 0L : cycleTime.getDeployTime();
        }
        return AverageLeadTime.builder()
                .averageValue(averageValueWithOneDecimal(cumulatedLeadTimeValue, pullRequestSize))
                .averageCodingTime(averageValueWithOneDecimal(cumulatedCodingTime, pullRequestSize))
                .averageReviewLag(averageValueWithOneDecimal(cumulatedReviewLag, pullRequestSize))
                .averageReviewTime(averageValueWithOneDecimal(cumulatedReviewTime, pullRequestSize))
                .averageDeployTime(averageValueWithOneDecimal(cumulatedDeployTime, pullRequestSize))
                .build();
    }

    public static AverageLeadTime computeLeadTimeForTagRegexToDeploySettings(final List<PullRequestView> pullRequestViewsToComputeLeadTime,
                                                                             final List<Tag> tagsMatchedToDeploy,
                                                                             final List<Commit> allCommitsFromStartDate,
                                                                             int pullRequestSize) {
        Long cumulatedLeadTimeValue = 0L;
        Long cumulatedCodingTime = 0L;
        Long cumulatedReviewLag = 0L;
        Long cumulatedReviewTime = 0L;
        Long cumulatedDeployTime = 0L;
        for (PullRequestView pullRequestWithCommitsView : pullRequestViewsToComputeLeadTime) {
            final CycleTime cycleTime =
                    CycleTime.computeLeadTimeForTagRegexToDeploySettings(pullRequestWithCommitsView,
                            tagsMatchedToDeploy, allCommitsFromStartDate);
            cumulatedCodingTime += cycleTime.getCodingTime();
            cumulatedReviewLag += cycleTime.getReviewLag();
            cumulatedReviewTime += cycleTime.getReviewTime();
            cumulatedLeadTimeValue += cycleTime.getValue();
            cumulatedDeployTime += Objects.isNull(cycleTime.getDeployTime()) ? 0L : cycleTime.getDeployTime();
        }
        return AverageLeadTime.builder()
                .averageValue(averageValueWithOneDecimal(cumulatedLeadTimeValue, pullRequestSize))
                .averageCodingTime(averageValueWithOneDecimal(cumulatedCodingTime, pullRequestSize))
                .averageReviewLag(averageValueWithOneDecimal(cumulatedReviewLag, pullRequestSize))
                .averageReviewTime(averageValueWithOneDecimal(cumulatedReviewTime, pullRequestSize))
                .averageDeployTime(averageValueWithOneDecimal(cumulatedDeployTime, pullRequestSize))
                .build();
    }


}
