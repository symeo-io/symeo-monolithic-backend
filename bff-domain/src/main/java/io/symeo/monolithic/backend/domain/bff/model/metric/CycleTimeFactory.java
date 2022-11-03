package io.symeo.monolithic.backend.domain.bff.model.metric;

import io.symeo.monolithic.backend.domain.bff.model.vcs.*;
import io.symeo.monolithic.backend.domain.helper.DateHelper;

import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Objects.isNull;

public class CycleTimeFactory {

    public CycleTime computeCycleTimeForMergeOnPullRequestMatchingDeliverySettings(final PullRequestView pullRequestView,
                                                                                   final List<PullRequestView> pullRequestViewsMatchingDeliverySettings,
                                                                                   final List<CommitView> allCommits) {
        final CommitHistory commitHistory = CommitHistory.initializeFromCommits(allCommits);
        final Date deployDate = computeDeployDateWithPullRequestMatchingDeliverySettings(pullRequestView,
                pullRequestViewsMatchingDeliverySettings,
                commitHistory);

        return computeCycleTimeWithTimeToDeploySupplier(
                pullRequestView,
                commitHistory,
                deployDate,
                computeTimeToDeployGivenDeployDate(
                        pullRequestView,
                        commitHistory,
                        deployDate
                )
        );
    }

    public CycleTime computeCycleTimeForTagRegexToDeploySettings(PullRequestView pullRequestView,
                                                                 final List<TagView> tagsMatchedToDeploy,
                                                                 final List<CommitView> allCommits) {
        final CommitHistory commitHistory = CommitHistory.initializeFromCommits(allCommits);
        final Date deployDate = computeDeployDateWithTagRegexMatchingDeploySettings(pullRequestView, tagsMatchedToDeploy,
                commitHistory);

        return computeCycleTimeWithTimeToDeploySupplier(
                pullRequestView,
                commitHistory,
                deployDate,
                computeTimeToDeployGivenDeployDate(
                        pullRequestView,
                        commitHistory,
                        deployDate
                )
        );
    }

    private CycleTime computeCycleTimeWithTimeToDeploySupplier(PullRequestView pullRequestView,
                                                               final CommitHistory commitHistory,
                                                               final Date deployDate,
                                                               final Supplier<Long> timeToDeploySupplier) {

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
        final Long timeToDeploy = timeToDeploySupplier.get();
        return CycleTime.builder()
                .codingTime(codingTime)
                .reviewTime(reviewTime)
                .timeToDeploy(timeToDeploy)
                .value(getCycleTimeValue(codingTime, reviewTime, timeToDeploy))
                .deployDate(deployDate)
                .pullRequestView(pullRequestView)
                .build();
    }

    private Long computeReviewTime(final List<CommitView> commitsOrderByDate,
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

    private Long computeCodingTime(final List<CommitView> commitsOrderByDate,
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

    private CommitView getLastCommitBeforeFirstReview(List<CommitView> commitsOrderByDate,
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

    private Long getCycleTimeValue(Long codingTime, Long reviewTime, Long timeToDeploy) {
        if (isNull(codingTime) && isNull(reviewTime) && isNull(timeToDeploy)) {
            return null;
        }
        return zeroIfNull(codingTime) + zeroIfNull(reviewTime) + zeroIfNull(timeToDeploy);
    }

    private Long zeroIfNull(final Long value) {
        return isNull(value) ? 0L : value;
    }

    private Date computeDeployDateWithPullRequestMatchingDeliverySettings(final PullRequestView pullRequestView,
                                                                          final List<PullRequestView> pullRequestViewsMatchingDeliverySettings,
                                                                          final CommitHistory commitHistory) {
        if (pullRequestViewsMatchingDeliverySettings.isEmpty()) {
            return null;
        }

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

        return isNull(firstMergeDateForCommitOnBranch) ? null : firstMergeDateForCommitOnBranch;
    }


    private Date computeDeployDateWithTagRegexMatchingDeploySettings(PullRequestView pullRequestView,
                                                                     List<TagView> tagsMatchedToDeploy,
                                                                     CommitHistory commitHistory) {
        if (tagsMatchedToDeploy.isEmpty()) {
            return null;
        }

        Date firstMergeDateForCommitOnBranch = null;
        for (TagView tagMatchedToDeploy : tagsMatchedToDeploy) {
            if (commitHistory.isCommitPresentOnMergeCommitHistory(pullRequestView.getMergeCommitSha(),
                    tagMatchedToDeploy.getCommitSha())) {
                final Date deployDate =
                        commitHistory.getCommitFromSha(tagMatchedToDeploy.getCommitSha()).getDate();
                if (isNull(firstMergeDateForCommitOnBranch)) {
                    firstMergeDateForCommitOnBranch = deployDate;
                } else if (deployDate.before(firstMergeDateForCommitOnBranch)) {
                    firstMergeDateForCommitOnBranch = deployDate;
                }
            }
        }

        return isNull(firstMergeDateForCommitOnBranch) ? null : firstMergeDateForCommitOnBranch;
    }

    private Supplier<Long> computeTimeToDeployGivenDeployDate(final PullRequestView pullRequestView,
                                                              final CommitHistory commitHistory,
                                                              final Date deployDate) {
        return () -> {
            final CommitView mergeCommit = commitHistory.getCommitFromSha(pullRequestView.getMergeCommitSha());
            return isNull(deployDate) ? null :
                    DateHelper.getNumberOfMinutesBetweenDates(mergeCommit.getDate(),
                            deployDate);
        };
    }
}
