package io.symeo.monolithic.backend.domain.model.insight;

import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Tag;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.IntStream;

import static java.time.temporal.ChronoUnit.*;
import static java.util.Objects.isNull;

@Builder(toBuilder = true)
@Value
public class Deployment {

    Integer deployCount;
    Float deploysPerDay;
    Float averageTimeBetweenDeploys;
    Float lastDeployDuration;
    String lastDeployRepository;

    public static Optional<Deployment> computeDeploymentForPullRequestMergedOnBranchRegexSettings(List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                                                                                                  Long numberOfDaysBetweenStartDateAndEndDate) {
        final int deployCount = pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate.size();
        final Float deploysPerDay = computeDeploysPerDay(pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate.size(),
                numberOfDaysBetweenStartDateAndEndDate);

        final List<Date> sortedDeployDateList = pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate.stream().map(PullRequestView::getMergeDate).sorted().toList();
        final Float averageTimeBetweenDeploys = computeAverageTimeBetweenDeploys(sortedDeployDateList);

        final Float lastDeployDuration = computeLastDeployDurationForPullRequestViewsMergedOnMatchedBranches(pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate);
        final String lastDeployRepository = computeLastDeployRepositoryForForPullRequestViewsMergedOnMatchedBranches(pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate);

        return Optional.of(Deployment.builder()
                .deployCount(deployCount)
                .deploysPerDay(deploysPerDay)
                .averageTimeBetweenDeploys(averageTimeBetweenDeploys)
                .lastDeployDuration(lastDeployDuration)
                .lastDeployRepository(lastDeployRepository)
                .build());
    }

    public static Optional<Deployment> computeDeploymentForTagRegexToDeploySettings(List<Commit> commitsMatchingTagRegexBetweenStartDateAndEndDate,
                                                                                    Long numberOfDaysBetweenStartDateAndEndDate,
                                                                                    List<Tag> tagsMatchingTeamIdAndDeployTagRegex) {
        final int deployCount = commitsMatchingTagRegexBetweenStartDateAndEndDate.size();
        final Float deploysPerDay = computeDeploysPerDay(commitsMatchingTagRegexBetweenStartDateAndEndDate.size(),
                numberOfDaysBetweenStartDateAndEndDate);

        final List<Date> sortedDeployDateList = commitsMatchingTagRegexBetweenStartDateAndEndDate.stream().map(Commit::getDate).sorted().toList();
        final Float averageTimeBetweenDeploys = computeAverageTimeBetweenDeploys(sortedDeployDateList);

        final Float lastDeployDuration = computeLastDeployDurationForTagRegexMatchingDeploySettings(commitsMatchingTagRegexBetweenStartDateAndEndDate);
        final String lastDeployRepository = computeLastDeployRepositoryForCommitsMatchingTagRegexBetweenStartDateAndEndDate(
                commitsMatchingTagRegexBetweenStartDateAndEndDate, tagsMatchingTeamIdAndDeployTagRegex);

        return Optional.of(Deployment.builder()
                .deployCount(deployCount)
                .deploysPerDay(deploysPerDay)
                .averageTimeBetweenDeploys(averageTimeBetweenDeploys)
                .lastDeployDuration(lastDeployDuration)
                .lastDeployRepository(lastDeployRepository)
                .build());
    }

    private static Float computeAverageTimeBetweenDeploys(List<Date> deployDatesList) {
        if (deployDatesList.size() >= 2) {
            return (float) IntStream.range(1, deployDatesList.size())
                    .mapToLong(i -> MINUTES.between(deployDatesList.get(i - 1).toInstant(), deployDatesList.get(i).toInstant()))
                    .average()
                    .getAsDouble();
        } else {
            return null;
        }
    }

    private static Float computeDeploysPerDay(int numberOfPullRequestOrCommitsMatchingDeploySettings, Long numberOfDaysBetweenStartDateAndEndDate) {
        return numberOfPullRequestOrCommitsMatchingDeploySettings != 0 ? Math.round(10f * numberOfPullRequestOrCommitsMatchingDeploySettings / numberOfDaysBetweenStartDateAndEndDate) / 10f : null;
    }

    private static Float computeLastDeployDurationForPullRequestViewsMergedOnMatchedBranches(List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate) {
        final LocalDateTime now = LocalDateTime.now();
        final PullRequestView lastDeployForPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate =
                pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate
                        .stream()
                        .max(Comparator.comparing(PullRequestView::getMergeDate))
                        .orElse(null);
        if (isNull(lastDeployForPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate)) {
            return null;
        } else {
            final Date lastDeployDate = lastDeployForPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate.getMergeDate();
            return isNull(lastDeployDate) ? null : (float) MINUTES.between(lastDeployDate.toInstant(), now.atZone(ZoneId.of("Europe/Paris")).toInstant());
        }
    }

    private static Float computeLastDeployDurationForTagRegexMatchingDeploySettings(List<Commit> commitsMatchingTagRegexBetweenStartDateAndEndDate) {
        final LocalDateTime now = LocalDateTime.now();
        final Commit lastDeployCommitForTagRegexMatchingDeploySettings =
                findLastDeployCommitInCommitsList(commitsMatchingTagRegexBetweenStartDateAndEndDate);
        if (isNull(lastDeployCommitForTagRegexMatchingDeploySettings)) {
            return null;
        } else {
            final Date lastDeployDate = lastDeployCommitForTagRegexMatchingDeploySettings.getDate();
            return isNull(lastDeployDate) ? null : (float) MINUTES.between(lastDeployDate.toInstant(), now.atZone(ZoneId.of("Europe/Paris")).toInstant());
        }
    }

    private static String computeLastDeployRepositoryForForPullRequestViewsMergedOnMatchedBranches(List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate) {
        return pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate
                .stream()
                .max(Comparator.comparing(PullRequestView::getMergeDate))
                .map(PullRequestView::getRepository)
                .orElse(null);
    }


    private static String computeLastDeployRepositoryForCommitsMatchingTagRegexBetweenStartDateAndEndDate(List<Commit> commitsMatchingTagRegexBetweenStartDateAndEndDate,
                                                                                                          List<Tag> tagsMatchingTeamIdAndDeployTagRegex) {
        final Commit lastDeployCommitForTagRegexMatchingDeploySettings =
                findLastDeployCommitInCommitsList(commitsMatchingTagRegexBetweenStartDateAndEndDate);
        if (isNull(lastDeployCommitForTagRegexMatchingDeploySettings)) {
            return null;
        } else {
            return tagsMatchingTeamIdAndDeployTagRegex
                    .stream()
                    .filter(tag -> tag.getRepository().getId().equals(lastDeployCommitForTagRegexMatchingDeploySettings.getRepositoryId()))
                    .findFirst()
                    .map(tag -> tag.getRepository().getName())
                    .orElse(null);
        }
    }

    private static Commit findLastDeployCommitInCommitsList(List<Commit> commitsList) {
        return commitsList
                .stream()
                .max(Comparator.comparing(Commit::getDate))
                .orElse(null);
    }
}
