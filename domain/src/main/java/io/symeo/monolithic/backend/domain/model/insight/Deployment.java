package io.symeo.monolithic.backend.domain.model.insight;

import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import lombok.Builder;
import lombok.Value;

import java.util.*;

@Builder(toBuilder = true)
@Value
public class Deployment {

    Integer deployCount;
    Float deploysPerDay;
    Float averageTimeBetweenDeploys;
    Float lastDeploy;
    String lastDeployRepository;

    public static Optional<Deployment> computeDeploymentForPullRequestMergedOnBranchRegexSettings(List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                                                                                                  Long numberOfDaysBetweenStartDateAndEndDate) {
        final int deployCount = pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate.size();
        final Float deploysPerDay = computeDeploysPerDay(pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate.size(),
                numberOfDaysBetweenStartDateAndEndDate);

        return Optional.of(Deployment.builder()
                .deployCount(deployCount)
                .deploysPerDay(deploysPerDay)
                .build());
    }

    public static Optional<Deployment> computeDeploymentForTagRegexToDeploySettings(List<Commit> commitsMatchingTagRegexBetweenStartDateAndEndDate,
                                                                                    Long numberOfDaysBetweenStartDateAndEndDate) {
        final int deployCount = commitsMatchingTagRegexBetweenStartDateAndEndDate.size();
        final Float deploysPerDay = computeDeploysPerDay(commitsMatchingTagRegexBetweenStartDateAndEndDate.size(),
                numberOfDaysBetweenStartDateAndEndDate);

        return Optional.of(Deployment.builder()
                .deployCount(deployCount)
                .deploysPerDay(deploysPerDay)
                .build());
    }

    private static Float computeDeploysPerDay(int numberOfPullRequestOrCommitsMatchingDeploySettings, Long numberOfDaysBetweenStartDateAndEndDate) {
        return Math.round(10f * numberOfPullRequestOrCommitsMatchingDeploySettings / numberOfDaysBetweenStartDateAndEndDate) / 10f;
    }
}
