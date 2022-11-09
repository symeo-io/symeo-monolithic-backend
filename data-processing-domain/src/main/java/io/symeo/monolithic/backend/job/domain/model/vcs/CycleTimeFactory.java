package io.symeo.monolithic.backend.job.domain.model.vcs;

import io.symeo.monolithic.backend.domain.helper.DateHelper;

import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Objects.isNull;

public class CycleTimeFactory {
    public CycleTime computeCycleTimeForMergeOnPullRequestMatchingDeliverySettings(PullRequest pullRequest,
                                                                                   List<PullRequest> pullRequestsMergedOnMatchedBranches,
                                                                                   List<Commit> commitsForRepository) {
        final CommitHistory commitHistory = CommitHistory.initializeFromCommits(commitsForRepository);
        final Date deployDate = computeDeployDateWithPullRequestMatchingDeliverySettings(pullRequest,
                pullRequestsMergedOnMatchedBranches,
                commitHistory);

        return computeCycleTimeWithTimeToDeploySupplier(
                pullRequest,
                commitHistory,
                deployDate,
                computeTimeToDeployGivenDeployDate(
                        pullRequest,
                        commitHistory,
                        deployDate
                )
        );
    }

    public CycleTime computeCycleTimeForTagRegexToDeploySettings(PullRequest pullRequest,
                                                                 final List<Tag> tagsMatchedToDeploy,
                                                                 final List<Commit> allCommits) {
        final CommitHistory commitHistory = CommitHistory.initializeFromCommits(allCommits);
        final Date deployDate = computeDeployDateWithTagRegexMatchingDeploySettings(pullRequest, tagsMatchedToDeploy,
                commitHistory);

        return computeCycleTimeWithTimeToDeploySupplier(
                pullRequest,
                commitHistory,
                deployDate,
                computeTimeToDeployGivenDeployDate(
                        pullRequest,
                        commitHistory,
                        deployDate
                )
        );
    }


    private CycleTime computeCycleTimeWithTimeToDeploySupplier(PullRequest pullRequest,
                                                               final CommitHistory commitHistory,
                                                               final Date deployDate,
                                                               final Supplier<Long> timeToDeploySupplier) {

        pullRequest = pullRequest.toBuilder()
                .commits(pullRequest.getCommitShaList().stream().map(commitHistory::getCommitFromSha).toList())
                .build();
        final List<Comment> commentsOrderByDate = pullRequest.getCommentsOrderByDate();
        final List<Commit> commitsOrderByDate = pullRequest.getCommitsOrderByDate();
        if (commitsOrderByDate.size() == 0) {
            return CycleTime.builder()
                    .pullRequest(pullRequest)
                    .build();
        }
        final Date mergeDate = pullRequest.getMergeDate();
        final Long codingTime = computeCodingTime(commitsOrderByDate, commentsOrderByDate);
        final Long reviewTime = computeReviewTime(commitsOrderByDate, commentsOrderByDate, mergeDate);
        final Long timeToDeploy = timeToDeploySupplier.get();
        return CycleTime.builder()
                .codingTime(codingTime)
                .reviewTime(reviewTime)
                .timeToDeploy(timeToDeploy)
                .value(getCycleTimeValue(codingTime, reviewTime, timeToDeploy))
                .deployDate(deployDate)
                .pullRequest(pullRequest)
                .build();
    }

    private Long computeReviewTime(final List<Commit> commitsOrderByDate,
                                   final List<Comment> commentsOrderByDate,
                                   final Date mergeDate) {
        final Date reviewTimeEndDate = isNull(mergeDate) ? new Date() : mergeDate;
        if (!commentsOrderByDate.isEmpty()) {
            final Commit lastCommitBeforeFirstReview = getLastCommitBeforeFirstReview(commitsOrderByDate,
                    commentsOrderByDate);
            return DateHelper.getNumberOfMinutesBetweenDates(lastCommitBeforeFirstReview.getDate(), reviewTimeEndDate);
        } else {
            return DateHelper.getNumberOfMinutesBetweenDates(commitsOrderByDate.get(commitsOrderByDate.size() - 1).getDate(),
                    reviewTimeEndDate);
        }
    }

    private Long computeCodingTime(final List<Commit> commitsOrderByDate,
                                   final List<Comment> commentsOrderByDate) {
        if (commitsOrderByDate.size() == 1) {
            return null;
        }
        final Commit firstCommit = commitsOrderByDate.get(0);
        if (!commentsOrderByDate.isEmpty()) {
            final Commit lastCommitBeforeFirstReview = getLastCommitBeforeFirstReview(commitsOrderByDate,
                    commentsOrderByDate);
            return DateHelper.getNumberOfMinutesBetweenDates(firstCommit.getDate(),
                    lastCommitBeforeFirstReview.getDate());
        }
        return DateHelper.getNumberOfMinutesBetweenDates(firstCommit.getDate(),
                commitsOrderByDate.get(commitsOrderByDate.size() - 1).getDate());
    }

    private Commit getLastCommitBeforeFirstReview(List<Commit> commitsOrderByDate,
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

    private Long getCycleTimeValue(Long codingTime, Long reviewTime, Long timeToDeploy) {
        if (isNull(codingTime) && isNull(reviewTime) && isNull(timeToDeploy)) {
            return null;
        }
        return zeroIfNull(codingTime) + zeroIfNull(reviewTime) + zeroIfNull(timeToDeploy);
    }

    private Long zeroIfNull(final Long value) {
        return isNull(value) ? 0L : value;
    }

    private Date computeDeployDateWithPullRequestMatchingDeliverySettings(final PullRequest pullRequestView,
                                                                          final List<PullRequest> pullRequestViewsMatchingDeliverySettings,
                                                                          final CommitHistory commitHistory) {
        if (pullRequestViewsMatchingDeliverySettings.isEmpty()) {
            return null;
        }

        Date firstMergeDateForCommitOnBranch = null;
        for (PullRequest pullRequestViewsMatchingDeliverySetting :
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

    private Date computeDeployDateWithTagRegexMatchingDeploySettings(PullRequest pullRequest,
                                                                     List<Tag> tagsMatchedToDeploy,
                                                                     CommitHistory commitHistory) {
        if (tagsMatchedToDeploy.isEmpty()) {
            return null;
        }

        Date firstMergeDateForCommitOnBranch = null;
        for (Tag tagMatchedToDeploy : tagsMatchedToDeploy) {
            if (commitHistory.isCommitPresentOnMergeCommitHistory(pullRequest.getMergeCommitSha(),
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


    private Supplier<Long> computeTimeToDeployGivenDeployDate(final PullRequest pullRequestView,
                                                              final CommitHistory commitHistory,
                                                              final Date deployDate) {
        return () -> {
            final Commit mergeCommit = commitHistory.getCommitFromSha(pullRequestView.getMergeCommitSha());
            return isNull(deployDate) ? null :
                    DateHelper.getNumberOfMinutesBetweenDates(mergeCommit.getDate(),
                            deployDate);
        };
    }
}
