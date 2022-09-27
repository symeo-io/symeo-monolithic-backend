package io.symeo.monolithic.backend.domain.model.insight;

import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Comment;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.domain.model.platform.vcs.CommitHistory;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Tag;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.getNumberOfMinutesBetweenDates;
import static io.symeo.monolithic.backend.domain.model.platform.vcs.CommitHistory.initializeFromCommits;
import static java.util.Objects.isNull;

@Builder(toBuilder = true)
@Value
@Slf4j
public class CycleTime {

    Long value;
    Long codingTime;
    Long reviewLag;
    Long reviewTime;
    Long deployTime;
    // To display PR data on graphs
    PullRequestView pullRequestView;

    private static Long computeReviewTime(final List<Commit> commitsOrderByDate,
                                          final List<Comment> commentsOrderByDate,
                                          final Date mergeDate) {

        if (!commentsOrderByDate.isEmpty()) {
            final Comment lastComment = commentsOrderByDate.get(commentsOrderByDate.size() - 1);
            return getNumberOfMinutesBetweenDates(lastComment.getCreationDate(), mergeDate);
        } else {
            if (isNull(mergeDate)) {
                return null;
            }
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
            if (isNull(mergeDate)) {
                return getNumberOfMinutesBetweenDates(commitsOrderByDate.get(commitsOrderByDate.size() - 1).getDate(),
                        new Date());
            } else {
                return null;
            }
        }
    }

    private static Long getDefaultReviewTimeMinutes() {
        return 60L;
    }

    private static Long computeCodingTime(final List<Commit> commitsOrderByDate) {
        if (commitsOrderByDate.size() == 1) {
            return null;
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

    public static CycleTime computeLeadTimeForMergeOnPullRequestMatchingDeliverySettings(PullRequestView pullRequestView,
                                                                                         final List<PullRequestView> pullRequestViewsMatchingDeliverySettings,
                                                                                         final List<Commit> allCommits) {
        final CommitHistory commitHistory = initializeFromCommits(allCommits);
        pullRequestView = pullRequestView.toBuilder()
                .commits(pullRequestView.getCommitShaList().stream().map(commitHistory::getCommitFromSha).toList())
                .build();
        final List<Comment> commentsOrderByDate = pullRequestView.getCommentsOrderByDate();
        final List<Commit> commitsOrderByDate = pullRequestView.getCommitsOrderByDate();
        final Date mergeDate = pullRequestView.getMergeDate();
        final Long codingTime = computeCodingTime(commitsOrderByDate);
        final Long reviewLag = computeReviewLag(commitsOrderByDate, commentsOrderByDate, mergeDate);
        final Long reviewTime = computeReviewTime(commitsOrderByDate, commentsOrderByDate, mergeDate);
        final Long deployTime =
                computeDeployTimeWithPullRequestMatchingDeliverySettings(pullRequestView,
                        pullRequestViewsMatchingDeliverySettings, commitHistory);
        return CycleTime.builder()
                .codingTime(codingTime)
                .reviewLag(reviewLag)
                .reviewTime(reviewTime)
                .deployTime(deployTime)
                .value(zeroIfNull(codingTime) + zeroIfNull(reviewLag) + zeroIfNull(reviewTime) + zeroIfNull(deployTime))
                .pullRequestView(pullRequestView)
                .build();
    }

    private static Long zeroIfNull(final Long value) {
        return isNull(value) ? 0L : value;
    }

    public static CycleTime computeLeadTimeForTagRegexToDeploySettings(PullRequestView pullRequestView,
                                                                       final List<Tag> tagsMatchedToDeploy,
                                                                       final List<Commit> allCommits) {
        final CommitHistory commitHistory = initializeFromCommits(allCommits);
        pullRequestView = pullRequestView.toBuilder()
                .commits(pullRequestView.getCommitShaList().stream().map(commitHistory::getCommitFromSha).toList())
                .build();
        final List<Comment> commentsOrderByDate = pullRequestView.getCommentsOrderByDate();
        final List<Commit> commitsOrderByDate = pullRequestView.getCommitsOrderByDate();
        final Date mergeDate = pullRequestView.getMergeDate();
        final Long codingTime = computeCodingTime(commitsOrderByDate);
        final Long reviewLag = computeReviewLag(commitsOrderByDate, commentsOrderByDate, mergeDate);
        final Long reviewTime = computeReviewTime(commitsOrderByDate, commentsOrderByDate, mergeDate);
        final Long deployTime =
                computeDeployTimeForTagRegexToDeploySettings(pullRequestView,
                        tagsMatchedToDeploy, commitHistory);
        return CycleTime.builder()
                .codingTime(codingTime)
                .reviewLag(reviewLag)
                .reviewTime(reviewTime)
                .deployTime(deployTime)
                .value(codingTime + reviewLag + reviewTime + (isNull(deployTime) ? 0L : deployTime))
                .pullRequestView(pullRequestView)
                .build();
    }


    private static Long computeDeployTimeWithPullRequestMatchingDeliverySettings(final PullRequestView pullRequestView,
                                                                                 final List<PullRequestView> pullRequestViewsMatchingDeliverySettings,
                                                                                 final CommitHistory commitHistory) {
        if (pullRequestViewsMatchingDeliverySettings.isEmpty()) {
            return null;
        }

        final Commit mergeCommit = commitHistory.getCommitFromSha(pullRequestView.getMergeCommitSha());
        Date firstMergeDateForCommitOnBranch = null;
        for (PullRequestView pullRequestViewsMatchingDeliverySetting : pullRequestViewsMatchingDeliverySettings) {
            if (commitHistory.isCommitPresentOnMergeCommitHistory(pullRequestView.getMergeCommitSha(),
                    pullRequestViewsMatchingDeliverySetting.getMergeCommitSha())) {
                final Date mergeDate = pullRequestViewsMatchingDeliverySetting.getMergeDate();
                if (isNull(firstMergeDateForCommitOnBranch)) {
                    firstMergeDateForCommitOnBranch = mergeDate;
                } else if (mergeDate.before(firstMergeDateForCommitOnBranch)) {
                    firstMergeDateForCommitOnBranch = mergeDate;
                }
            }
        }
        return isNull(firstMergeDateForCommitOnBranch) ? null : getNumberOfMinutesBetweenDates(mergeCommit.getDate(),
                firstMergeDateForCommitOnBranch);
    }

    private static Long computeDeployTimeForTagRegexToDeploySettings(final PullRequestView pullRequestView,
                                                                     final List<Tag> tagsMatchedToDeploy,
                                                                     final CommitHistory commitHistory) {
        if (tagsMatchedToDeploy.isEmpty()) {
            return null;
        }

        final Commit mergeCommit = commitHistory.getCommitFromSha(pullRequestView.getMergeCommitSha());
        Date firstMergeDateForCommitOnBranch = null;
        for (Tag tagMatchedToDeploy : tagsMatchedToDeploy) {
            if (commitHistory.isCommitPresentOnMergeCommitHistory(pullRequestView.getMergeCommitSha(),
                    tagMatchedToDeploy.getCommitSha())) {
                final Date mergeDate = commitHistory.getCommitFromSha(tagMatchedToDeploy.getCommitSha()).getDate();
                if (isNull(firstMergeDateForCommitOnBranch)) {
                    firstMergeDateForCommitOnBranch = mergeDate;
                } else if (mergeDate.before(firstMergeDateForCommitOnBranch)) {
                    firstMergeDateForCommitOnBranch = mergeDate;
                }
            }
        }
        return isNull(firstMergeDateForCommitOnBranch) ? null : getNumberOfMinutesBetweenDates(mergeCommit.getDate(),
                firstMergeDateForCommitOnBranch);
    }


}
