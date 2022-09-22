package io.symeo.monolithic.backend.domain.model.insight;

import io.symeo.monolithic.backend.domain.model.account.settings.DeployDetectionSettings;
import io.symeo.monolithic.backend.domain.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Comment;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.domain.model.platform.vcs.CommitHistory;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.getNumberOfMinutesBetweenDates;
import static io.symeo.monolithic.backend.domain.model.platform.vcs.CommitHistory.initializeFromCommits;
import static java.util.Objects.isNull;

@Builder(toBuilder = true)
@Value
@Slf4j
public class LeadTime {

    Long value;
    Long codingTime;
    Long reviewLag;
    Long reviewTime;
    Long deployTime;
    // To display PR data on graphs
    PullRequestView pullRequestView;


    public static LeadTime computeLeadTimeForPullRequestView(final PullRequestView pullRequestView) {
        final List<Comment> commentsOrderByDate = pullRequestView.getCommentsOrderByDate();
        final List<Commit> commitsOrderByDate = pullRequestView.getCommitsOrderByDate();
        final Date mergeDate = pullRequestView.getMergeDate();
        final Long codingTime = computeCodingTime(commitsOrderByDate);
        final Long reviewLag = computeReviewLag(commitsOrderByDate, commentsOrderByDate, mergeDate);
        final Long reviewTime = computeReviewTime(commitsOrderByDate, commentsOrderByDate, mergeDate);
//        final Long deployTime = computeDeployTime();
        return LeadTime.builder()
                .codingTime(codingTime)
                .reviewLag(reviewLag)
                .reviewTime(reviewTime)
//                .deployTime(deployTime)
                .value(codingTime + reviewLag + reviewTime)
                .pullRequestView(pullRequestView)
                .build();
    }

    private static Long computeReviewTime(final List<Commit> commitsOrderByDate,
                                          final List<Comment> commentsOrderByDate,
                                          final Date mergeDate) {
        if (!commentsOrderByDate.isEmpty()) {
            final Comment lastComment = commentsOrderByDate.get(commentsOrderByDate.size() - 1);
            return getNumberOfMinutesBetweenDates(lastComment.getCreationDate(), mergeDate);
        } else {
            final Long minutesBetweenLastCommitAndMerge =
                    getNumberOfMinutesBetweenDates(commitsOrderByDate.get(commitsOrderByDate.size() - 1).getDate(),
                            mergeDate);
            if (minutesBetweenLastCommitAndMerge <= getDefaultReviewTimeMinutes()) {
                return minutesBetweenLastCommitAndMerge;
            } else {
                return getDefaultReviewTimeMinutes();
            }
        }
    }

    private static Long computeReviewLag(final List<Commit> commitsOrderByDate,
                                         final List<Comment> commentsOrderByDate, final Date mergeDate) {
        if (!commentsOrderByDate.isEmpty()) {
            final Comment firstComment = commentsOrderByDate.get(0);
            final Commit lastCommitBeforeFirstReview = getLastCommitBeforeFirstReview(commitsOrderByDate,
                    commentsOrderByDate);
            return getNumberOfMinutesBetweenDates(lastCommitBeforeFirstReview.getDate(),
                    firstComment.getCreationDate());
        } else {
            final Long minutesBetweenLastCommitAndMerge =
                    getNumberOfMinutesBetweenDates(commitsOrderByDate.get(commitsOrderByDate.size() - 1).getDate(),
                            mergeDate);
            if (minutesBetweenLastCommitAndMerge <= getDefaultReviewTimeMinutes()) {
                return 0L;
            } else {
                return minutesBetweenLastCommitAndMerge - getDefaultReviewTimeMinutes();
            }
        }
    }

    private static Long getDefaultReviewTimeMinutes() {
        return 60L;
    }

    private static Long computeCodingTime(final List<Commit> commitsOrderByDate) {
        if (commitsOrderByDate.size() == 1) {
            return 0L;
        }
        return getNumberOfMinutesBetweenDates(commitsOrderByDate.get(0).getDate(),
                commitsOrderByDate.get(commitsOrderByDate.size() - 1).getDate());
    }

    private static Commit getLastCommitBeforeFirstReview(List<Commit> commitsOrderByDate,
                                                         List<Comment> commentsOrderByDate) {
        Commit lastCommitBeforeFirstReview;
        if (!commentsOrderByDate.isEmpty()) {
            final Comment firstComment = commentsOrderByDate.get(0);
            if (firstComment.getCreationDate().before(commitsOrderByDate.get(0).getDate())) {
                lastCommitBeforeFirstReview = commitsOrderByDate.get(0);
            } else {
                lastCommitBeforeFirstReview = commitsOrderByDate.get(0);
                for (Commit commit : commitsOrderByDate) {
                    if (commit.getDate().before(firstComment.getCreationDate())) {
                        lastCommitBeforeFirstReview = commit;
                    }
                }
            }
        } else {
            lastCommitBeforeFirstReview = commitsOrderByDate.get(commitsOrderByDate.size() - 1);
        }
        return lastCommitBeforeFirstReview;
    }

    public static LeadTime computeLeadTimeForDeliverySettingsAndPullRequestViewAndAllCommits(final OrganizationSettings organizationSettings,
                                                                                             final PullRequestView pullRequestView,
                                                                                             final List<Commit> allCommits) {
        final List<Comment> commentsOrderByDate = pullRequestView.getCommentsOrderByDate();
        final List<Commit> commitsOrderByDate = pullRequestView.getCommitsOrderByDate();
        final Date mergeDate = pullRequestView.getMergeDate();
        final Long codingTime = computeCodingTime(commitsOrderByDate);
        final Long reviewLag = computeReviewLag(commitsOrderByDate, commentsOrderByDate, mergeDate);
        final Long reviewTime = computeReviewTime(commitsOrderByDate, commentsOrderByDate, mergeDate);
        final Long deployTime =
                computeDeployTimeForMergeOnBranchRegex(organizationSettings.getDeliverySettings().getDeployDetectionSettings(),
                        pullRequestView, allCommits);
        return LeadTime.builder()
                .codingTime(codingTime)
                .reviewLag(reviewLag)
                .reviewTime(reviewTime)
                .deployTime(deployTime)
                .value(codingTime + reviewLag + reviewTime + (isNull(deployTime) ? 0L : deployTime))
                .pullRequestView(pullRequestView)
                .build();
    }

    private static Long computeDeployTimeForMergeOnBranchRegex(DeployDetectionSettings deployDetectionSettings,
                                                               PullRequestView pullRequestView,
                                                               List<Commit> allCommits) {
        final String pullRequestMergedOnBranchRegex = deployDetectionSettings.getPullRequestMergedOnBranchRegex();
        final Pattern branchPattern = Pattern.compile(pullRequestMergedOnBranchRegex);
        final CommitHistory commitHistory = initializeFromCommits(allCommits);
        final Commit mergeCommit = commitHistory.getCommitFromSha(pullRequestView.getMergeCommitSha());
        final List<String> matchedBranches = commitHistory.getAllBranches().stream()
                .filter(branch -> branchPattern.matcher(branch).find())
                .toList();
        if (matchedBranches.isEmpty()) {
            return null;
        } else {
            Date firstMergeDateForCommitOnBranch = null;
            for (String matchedBranch : matchedBranches) {
                final Date mergeDateForCommitOnBranch = commitHistory.getMergeDateForCommitOnBranch(mergeCommit,
                        matchedBranch);
                if (isNull(firstMergeDateForCommitOnBranch)) {
                    firstMergeDateForCommitOnBranch = mergeDateForCommitOnBranch;
                } else {
                    if (mergeDateForCommitOnBranch.before(firstMergeDateForCommitOnBranch)) {
                        LOGGER.warn("Multiple branches {} were found for regex {}", String.join(", ",
                                matchedBranches), pullRequestMergedOnBranchRegex);
                        firstMergeDateForCommitOnBranch = mergeDateForCommitOnBranch;
                    }
                }
            }
            return getNumberOfMinutesBetweenDates(mergeCommit.getDate(), firstMergeDateForCommitOnBranch);
        }
    }
}
