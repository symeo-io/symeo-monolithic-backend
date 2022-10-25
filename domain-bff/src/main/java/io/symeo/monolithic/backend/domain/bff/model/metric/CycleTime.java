package io.symeo.monolithic.backend.domain.bff.model.metric;

import io.symeo.monolithic.backend.domain.bff.model.vcs.*;
import io.symeo.monolithic.backend.domain.helper.DateHelper;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Objects.isNull;

@Builder(toBuilder = true)
@Value
@Slf4j
public class CycleTime {

    Long value;
    Long codingTime;
    Long reviewTime;
    Long deployTime;
    // To display PR data on graphs
    PullRequestView pullRequestView;

    public static CycleTime computeCycleTimeForMergeOnPullRequestMatchingDeliverySettings(final PullRequestView pullRequestView,
                                                                                          final List<PullRequestView> pullRequestViewsMatchingDeliverySettings,
                                                                                          final List<CommitView> allCommits) {
        final CommitHistory commitHistory = CommitHistory.initializeFromCommits(allCommits);
        return computeCycleTimeWithDeployTimeSupplier(
                pullRequestView,
                commitHistory,
                computeDeployTimeWithPullRequestMatchingDeliverySettingsSupplier(
                        pullRequestView,
                        pullRequestViewsMatchingDeliverySettings,
                        commitHistory
                )
        );
    }

    public static CycleTime computeCycleTimeForTagRegexToDeploySettings(PullRequestView pullRequestView,
                                                                        final List<TagView> tagsMatchedToDeploy,
                                                                        final List<CommitView> allCommits) {
        final CommitHistory commitHistory = CommitHistory.initializeFromCommits(allCommits);
        return computeCycleTimeWithDeployTimeSupplier(
                pullRequestView,
                commitHistory,
                computeDeployTimeForTagRegexToDeploySettingsSupplier(
                        pullRequestView,
                        tagsMatchedToDeploy,
                        commitHistory
                )
        );
    }

    private static CycleTime computeCycleTimeWithDeployTimeSupplier(PullRequestView pullRequestView,
                                                                    final CommitHistory commitHistory,
                                                                    final Supplier<Long> deployTimeSupplier) {

        pullRequestView = pullRequestView.toBuilder()
                .commits(pullRequestView.getCommitShaList().stream().map(commitHistory::getCommitFromSha).toList())
                .build();
        final List<CommentView> commentsOrderByDate = pullRequestView.getCommentsOrderByDate();
        final List<CommitView> commitsOrderByDate = pullRequestView.getCommitsOrderByDate();
        if (commitsOrderByDate.size() == 0) {
            return CycleTime.builder()
                    .pullRequestView(pullRequestView)
                    .build();
        }
        final Date mergeDate = pullRequestView.getMergeDate();
        final Long codingTime = computeCodingTime(commitsOrderByDate, commentsOrderByDate);
        final Long reviewTime = computeReviewTime(commitsOrderByDate, commentsOrderByDate, mergeDate);
        final Long deployTime = deployTimeSupplier.get();
        return CycleTime.builder()
                .codingTime(codingTime)
                .reviewTime(reviewTime)
                .deployTime(deployTime)
                .value(getCycleTimeValue(codingTime, reviewTime, deployTime))
                .pullRequestView(pullRequestView)
                .build();
    }

    private static Long computeReviewTime(final List<CommitView> commitsOrderByDate,
                                          final List<CommentView> commentsOrderByDate,
                                          final Date mergeDate) {
        final Date reviewTimeEndDate = isNull(mergeDate) ? new Date() : mergeDate;
        if (!commentsOrderByDate.isEmpty()) {
            final CommitView lastCommitBeforeFirstReview = getLastCommitBeforeFirstReview(commitsOrderByDate,
                    commentsOrderByDate);
            return DateHelper.getNumberOfMinutesBetweenDates(lastCommitBeforeFirstReview.getDate(), reviewTimeEndDate);
        } else {
            return DateHelper.getNumberOfMinutesBetweenDates(commitsOrderByDate.get(commitsOrderByDate.size() - 1).getDate(),
                    reviewTimeEndDate);
        }
    }

    private static Long computeCodingTime(final List<CommitView> commitsOrderByDate,
                                          final List<CommentView> commentsOrderByDate) {
        if (commitsOrderByDate.size() == 1) {
            return null;
        }
        final CommitView firstCommit = commitsOrderByDate.get(0);
        if (!commentsOrderByDate.isEmpty()) {
            final CommitView lastCommitBeforeFirstReview = getLastCommitBeforeFirstReview(commitsOrderByDate,
                    commentsOrderByDate);
            return DateHelper.getNumberOfMinutesBetweenDates(firstCommit.getDate(),
                    lastCommitBeforeFirstReview.getDate());
        }
        return DateHelper.getNumberOfMinutesBetweenDates(firstCommit.getDate(),
                commitsOrderByDate.get(commitsOrderByDate.size() - 1).getDate());
    }

    private static CommitView getLastCommitBeforeFirstReview(List<CommitView> commitsOrderByDate,
                                                             List<CommentView> commentsOrderByDate) {
        CommitView lastCommitBeforeFirstReview;
        if (!commentsOrderByDate.isEmpty()) {
            final CommentView firstComment = commentsOrderByDate.get(0);
            if (firstComment.getCreationDate().before(commitsOrderByDate.get(0).getDate())) {
                lastCommitBeforeFirstReview = commitsOrderByDate.get(0);
            } else {
                lastCommitBeforeFirstReview = commitsOrderByDate.get(0);
                for (CommitView commit : commitsOrderByDate) {
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

    private static Long getCycleTimeValue(Long codingTime, Long reviewTime, Long deployTime) {
        if (isNull(codingTime) && isNull(reviewTime) && isNull(deployTime)) {
            return null;
        }
        return zeroIfNull(codingTime) + zeroIfNull(reviewTime) + zeroIfNull(deployTime);
    }

    private static Long zeroIfNull(final Long value) {
        return isNull(value) ? 0L : value;
    }

    private static Supplier<Long> computeDeployTimeWithPullRequestMatchingDeliverySettingsSupplier(
            final PullRequestView pullRequestView,
            final List<PullRequestView> pullRequestViewsMatchingDeliverySettings,
            final CommitHistory commitHistory
    ) {
        return () -> {
            if (pullRequestViewsMatchingDeliverySettings.isEmpty()) {
                return null;
            }

            final CommitView mergeCommit = commitHistory.getCommitFromSha(pullRequestView.getMergeCommitSha());
            Date firstMergeDateForCommitOnBranch = null;
            for (PullRequestView pullRequestViewsMatchingDeliverySetting :
                    pullRequestViewsMatchingDeliverySettings) {
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
            return isNull(firstMergeDateForCommitOnBranch) ? null :
                    DateHelper.getNumberOfMinutesBetweenDates(mergeCommit.getDate(),
                            firstMergeDateForCommitOnBranch);
        };
    }

    private static Supplier<Long> computeDeployTimeForTagRegexToDeploySettingsSupplier(final PullRequestView pullRequestView,
                                                                                       final List<TagView> tagsMatchedToDeploy,
                                                                                       final CommitHistory commitHistory) {
        return () -> {
            if (tagsMatchedToDeploy.isEmpty()) {
                return null;
            }
            final CommitView mergeCommit = commitHistory.getCommitFromSha(pullRequestView.getMergeCommitSha());
            Date firstMergeDateForCommitOnBranch = null;
            for (TagView tagMatchedToDeploy : tagsMatchedToDeploy) {
                if (commitHistory.isCommitPresentOnMergeCommitHistory(pullRequestView.getMergeCommitSha(),
                        tagMatchedToDeploy.getCommitSha())) {
                    final Date mergeDate =
                            commitHistory.getCommitFromSha(tagMatchedToDeploy.getCommitSha()).getDate();
                    if (isNull(firstMergeDateForCommitOnBranch)) {
                        firstMergeDateForCommitOnBranch = mergeDate;
                    } else if (mergeDate.before(firstMergeDateForCommitOnBranch)) {
                        firstMergeDateForCommitOnBranch = mergeDate;
                    }
                }
            }
            return isNull(firstMergeDateForCommitOnBranch) ? null :
                    DateHelper.getNumberOfMinutesBetweenDates(mergeCommit.getDate(),
                            firstMergeDateForCommitOnBranch);
        };
    }


}