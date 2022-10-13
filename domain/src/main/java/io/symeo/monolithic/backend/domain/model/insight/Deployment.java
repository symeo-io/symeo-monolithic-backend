package io.symeo.monolithic.backend.domain.model.insight;

import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import lombok.Builder;
import lombok.Value;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.IntStream;

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

        final List<Date> deployDateList = new ArrayList<>(pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate.stream().map(PullRequestView::getMergeDate).toList());
        Collections.sort(deployDateList);
        final Float averageTimeBetweenDeploys = computeAverageTimeBetweenDeploys(deployDateList);

        return Optional.of(Deployment.builder()
                .deployCount(deployCount)
                .deploysPerDay(deploysPerDay)
                .averageTimeBetweenDeploys(averageTimeBetweenDeploys)
                .build());
    }

    public static Optional<Deployment> computeDeploymentForTagRegexToDeploySettings(List<Commit> commitsMatchingTagRegexBetweenStartDateAndEndDate,
                                                                                    Long numberOfDaysBetweenStartDateAndEndDate) {
        final int deployCount = commitsMatchingTagRegexBetweenStartDateAndEndDate.size();
        final Float deploysPerDay = computeDeploysPerDay(commitsMatchingTagRegexBetweenStartDateAndEndDate.size(),
                numberOfDaysBetweenStartDateAndEndDate);

        final List<Date> deployDateList = new ArrayList<>(commitsMatchingTagRegexBetweenStartDateAndEndDate.stream().map(Commit::getDate).toList());
        Collections.sort(deployDateList);
        final Float averageTimeBetweenDeploys = computeAverageTimeBetweenDeploys(deployDateList);

        return Optional.of(Deployment.builder()
                .deployCount(deployCount)
                .deploysPerDay(deploysPerDay)
                .averageTimeBetweenDeploys(averageTimeBetweenDeploys)
                .build());
    }

    private static Float computeAverageTimeBetweenDeploys(List<Date> deployDatesList) {
        if (deployDatesList.size() >= 2) {
            return (float) IntStream.range(1, deployDatesList.size())
                    .mapToLong(i -> ChronoUnit.MINUTES.between(deployDatesList.get(i - 1).toInstant(), deployDatesList.get(i).toInstant()))
                    .average()
                    .getAsDouble();
        } else {
            return null;
        }
    }

    private static Float computeDeploysPerDay(int numberOfPullRequestOrCommitsMatchingDeploySettings, Long numberOfDaysBetweenStartDateAndEndDate) {
        return numberOfPullRequestOrCommitsMatchingDeploySettings != 0 ? Math.round(10f * numberOfPullRequestOrCommitsMatchingDeploySettings / numberOfDaysBetweenStartDateAndEndDate) / 10f : null;
    }
}
