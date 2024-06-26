package io.symeo.monolithic.backend.domain.bff.model.metric;

import io.symeo.monolithic.backend.domain.bff.model.vcs.CommitView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.TagView;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.Objects.isNull;

@Builder(toBuilder = true)
@Value
public class Deployment {

    Integer deployCount;
    Float deploysPerDay;
    Float averageTimeBetweenDeploys;
    Float lastDeployDuration;
    String lastDeployRepository;
    String lastDeployLink;

    public static Optional<Deployment> computeDeploymentForPullRequestMergedOnBranchRegexSettings(List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                                                                                                  Long numberOfDaysBetweenStartDateAndEndDate) {
        final int deployCount = pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate.size();
        final Float deploysPerDay =
                computeDeploysPerDay(pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate.size(),
                        numberOfDaysBetweenStartDateAndEndDate);

        final List<Date> sortedDeployDateList =
                pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate.stream().map(PullRequestView::getMergeDate).sorted().toList();
        final Float averageTimeBetweenDeploys = computeAverageTimeBetweenDeploys(sortedDeployDateList);

        final Float lastDeployDuration =
                computeLastDeployDurationForPullRequestViewsMergedOnMatchedBranches(pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate);
        final String lastDeployRepository =
                computeLastDeployRepositoryForForPullRequestViewsMergedOnMatchedBranches(pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate);
        final String lastDeployLink =
                computeLastDeployLinkForPullRequestViewsMergedOnMatchedBranches(pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate);

        return Optional.of(Deployment.builder()
                .deployCount(deployCount)
                .deploysPerDay(deploysPerDay)
                .averageTimeBetweenDeploys(averageTimeBetweenDeploys)
                .lastDeployDuration(lastDeployDuration)
                .lastDeployRepository(lastDeployRepository)
                .lastDeployLink(lastDeployLink)
                .build());
    }

    public static Optional<Deployment> computeDeploymentForTagRegexToDeploySettings(List<CommitView> commitsMatchingTagRegexBetweenStartDateAndEndDate,
                                                                                    Long numberOfDaysBetweenStartDateAndEndDate,
                                                                                    List<TagView> tagsMatchingTeamIdAndDeployTagRegex) {
        final int deployCount = commitsMatchingTagRegexBetweenStartDateAndEndDate.size();
        final Float deploysPerDay = computeDeploysPerDay(commitsMatchingTagRegexBetweenStartDateAndEndDate.size(),
                numberOfDaysBetweenStartDateAndEndDate);

        final List<Date> sortedDeployDateList =
                commitsMatchingTagRegexBetweenStartDateAndEndDate.stream().map(CommitView::getDate).sorted().toList();
        final Float averageTimeBetweenDeploys = computeAverageTimeBetweenDeploys(sortedDeployDateList);

        final Float lastDeployDuration =
                computeLastDeployDurationForTagRegexMatchingDeploySettings(commitsMatchingTagRegexBetweenStartDateAndEndDate);
        final String lastDeployRepository =
                computeLastDeployRepositoryForCommitsMatchingTagRegexBetweenStartDateAndEndDate(
                        commitsMatchingTagRegexBetweenStartDateAndEndDate, tagsMatchingTeamIdAndDeployTagRegex);
        final String lastDeployLink =
                computeLastDeployLinkForForCommitsMatchingTagRegexBetweenStartDateAndEndDate(commitsMatchingTagRegexBetweenStartDateAndEndDate,
                        tagsMatchingTeamIdAndDeployTagRegex);

        return Optional.of(Deployment.builder()
                .deployCount(deployCount)
                .deploysPerDay(deploysPerDay)
                .averageTimeBetweenDeploys(averageTimeBetweenDeploys)
                .lastDeployDuration(lastDeployDuration)
                .lastDeployRepository(lastDeployRepository)
                .lastDeployLink(lastDeployLink)
                .build());
    }

    private static Float computeAverageTimeBetweenDeploys(List<Date> deployDatesList) {
        if (deployDatesList.size() >= 2) {
            return (float) IntStream.range(1, deployDatesList.size())
                    .mapToLong(i -> MINUTES.between(deployDatesList.get(i - 1).toInstant(),
                            deployDatesList.get(i).toInstant()))
                    .average()
                    .getAsDouble();
        } else {
            return null;
        }
    }

    private static Float computeDeploysPerDay(int numberOfPullRequestOrCommitsMatchingDeploySettings,
                                              Long numberOfDaysBetweenStartDateAndEndDate) {
        return numberOfPullRequestOrCommitsMatchingDeploySettings != 0 ?
                Math.round(10 * numberOfPullRequestOrCommitsMatchingDeploySettings / (numberOfDaysBetweenStartDateAndEndDate * 1.0)) / 10.0f : null;
    }

    private static Float computeLastDeployDurationForPullRequestViewsMergedOnMatchedBranches(List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate) {
        final LocalDateTime now = LocalDateTime.now();
        final PullRequestView lastDeployForPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate =
                findLastDeployPullRequestViewForPullRequestViewList(pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate);
        if (isNull(lastDeployForPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate)) {
            return null;
        } else {
            final Date lastDeployDate =
                    lastDeployForPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate.getMergeDate();
            return isNull(lastDeployDate) ? null : (float) MINUTES.between(lastDeployDate.toInstant(),
                    now.atZone(ZoneId.of("Europe/Paris")).toInstant());
        }
    }

    private static Float computeLastDeployDurationForTagRegexMatchingDeploySettings(List<CommitView> commitsMatchingTagRegexBetweenStartDateAndEndDate) {
        final LocalDateTime now = LocalDateTime.now();
        final CommitView lastDeployCommitForTagRegexMatchingDeploySettings =
                findLastDeployCommitInCommitsList(commitsMatchingTagRegexBetweenStartDateAndEndDate);
        if (isNull(lastDeployCommitForTagRegexMatchingDeploySettings)) {
            return null;
        } else {
            final Date lastDeployDate = lastDeployCommitForTagRegexMatchingDeploySettings.getDate();
            return isNull(lastDeployDate) ? null : (float) MINUTES.between(lastDeployDate.toInstant(),
                    now.atZone(ZoneId.of("Europe/Paris")).toInstant());
        }
    }

    private static String computeLastDeployRepositoryForForPullRequestViewsMergedOnMatchedBranches(List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate) {
        return pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate
                .stream()
                .max(Comparator.comparing(PullRequestView::getMergeDate))
                .map(PullRequestView::getRepository)
                .orElse(null);
    }


    private static String computeLastDeployRepositoryForCommitsMatchingTagRegexBetweenStartDateAndEndDate(List<CommitView> commitsMatchingTagRegexBetweenStartDateAndEndDate,
                                                                                                          List<TagView> tagsMatchingTeamIdAndDeployTagRegex) {
        final CommitView lastDeployCommitForTagRegexMatchingDeploySettings =
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

    private static PullRequestView findLastDeployPullRequestViewForPullRequestViewList(List<PullRequestView> pullRequestViewList) {
        return pullRequestViewList
                .stream()
                .max(Comparator.comparing(PullRequestView::getMergeDate))
                .orElse(null);
    }

    private static CommitView findLastDeployCommitInCommitsList(List<CommitView> commitsList) {
        return commitsList
                .stream()
                .max(Comparator.comparing(CommitView::getDate))
                .orElse(null);
    }

    private static String computeLastDeployLinkForPullRequestViewsMergedOnMatchedBranches(List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate) {
        final PullRequestView lastDeployForPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate =
                findLastDeployPullRequestViewForPullRequestViewList(pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate);
        return isNull(lastDeployForPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate)
                ? null
                : lastDeployForPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate.getVcsUrl();
    }

    private static String computeLastDeployLinkForForCommitsMatchingTagRegexBetweenStartDateAndEndDate(List<CommitView> commitsMatchingTagRegexBetweenStartDateAndEndDate,
                                                                                                       List<TagView> tagsMatchingTeamIdAndDeployTagRegex) {
        final CommitView lastDeployCommitForTagRegexMatchingDeploySettings =
                findLastDeployCommitInCommitsList(commitsMatchingTagRegexBetweenStartDateAndEndDate);
        if (isNull(lastDeployCommitForTagRegexMatchingDeploySettings)) {
            return null;
        } else {
            return tagsMatchingTeamIdAndDeployTagRegex
                    .stream()
                    .filter(tag -> tag.getCommitSha().equals(lastDeployCommitForTagRegexMatchingDeploySettings.getSha()))
                    .findFirst()
                    .map(TagView::getVcsUrl)
                    .orElse(null);
        }
    }
}
