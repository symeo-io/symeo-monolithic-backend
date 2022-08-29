package io.symeo.monolithic.backend.domain.model.insight;

import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Comment;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import lombok.Builder;
import lombok.Value;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.getNumberOfMinutesBetweenDates;
import static java.lang.Math.round;
import static java.util.Optional.empty;
import static java.util.Optional.of;

@Value
@Builder
public class LeadTime {
    Float averageValue;
    Float averageCodingTime;
    Float averageReviewLag;
    Float averageReviewTime;
    Float averageDeployTime;

    public static Optional<LeadTime> buildFromPullRequestWithCommitsViews(final List<PullRequestView> pullRequestWithCommitsViews) {
        final List<PullRequestView> pullRequestViewsToComputeLeadTime =
                filterPullRequestForLeadTimeComputation(pullRequestWithCommitsViews);
        final int pullRequestSize = pullRequestViewsToComputeLeadTime.size();
        if (pullRequestSize == 0) {
            return empty();
        }
        return of(computeLeadTime(pullRequestViewsToComputeLeadTime, pullRequestSize));

    }

    private static List<PullRequestView> filterPullRequestForLeadTimeComputation(List<PullRequestView> pullRequestWithCommitsViews) {
        return pullRequestWithCommitsViews.stream()
                .filter(LeadTime::filterPullRequestToComputeLeadTime).toList();
    }

    private static LeadTime computeLeadTime(List<PullRequestView> pullRequestViewsToComputeLeadTime,
                                            int pullRequestSize) {
        Long cumulatedLeadTimeValue = 0L;
        Long cumulatedCodingTime = 0L;
        Long cumulatedReviewLag = 0L;
        Long cumulatedReviewTime = 0L;
        for (PullRequestView pullRequestWithCommitsView : pullRequestViewsToComputeLeadTime) {
            final SingleLeadTime singleLeadTime = computeLeadTimeForPullRequestView(pullRequestWithCommitsView);
            cumulatedCodingTime += singleLeadTime.getCodingTime();
            cumulatedReviewLag += singleLeadTime.getReviewLag();
            cumulatedReviewTime += singleLeadTime.getReviewTime();
            cumulatedLeadTimeValue += singleLeadTime.getValue();
        }
        return LeadTime.builder()
                .averageValue(averageValueWithOneDecimal(cumulatedLeadTimeValue, pullRequestSize))
                .averageCodingTime(averageValueWithOneDecimal(cumulatedCodingTime, pullRequestSize))
                .averageReviewLag(averageValueWithOneDecimal(cumulatedReviewLag, pullRequestSize))
                .averageReviewTime(averageValueWithOneDecimal(cumulatedReviewTime, pullRequestSize))
                .build();
    }

    private static boolean filterPullRequestToComputeLeadTime(final PullRequestView pullRequestView) {
        return !pullRequestView.getCommits().isEmpty() && pullRequestView.getStatus().equals(PullRequest.MERGE);
    }

    private static Float averageValueWithOneDecimal(Long cumulatedLeadTimeValue, int size) {
        return round(10 * cumulatedLeadTimeValue / size) / 10f;
    }

    private static SingleLeadTime computeLeadTimeForPullRequestView(final PullRequestView pullRequestView) {
        final List<Comment> commentsOrderByDate = pullRequestView.getCommentsOrderByDate();
        final List<Commit> commitsOrderByDate = pullRequestView.getCommitsOrderByDate();
        final Date mergeDate = pullRequestView.getMergeDate();
        final Date creationDate = pullRequestView.getCreationDate();
        final Commit lastCommitBeforeFirstReview =
                getLastCommitBeforeFirstReview(commitsOrderByDate,
                        commentsOrderByDate);
        final Commit lastCommit = commitsOrderByDate.get(commitsOrderByDate.size() - 1);
        final Long codingTime = computeCodingTime(creationDate, lastCommitBeforeFirstReview);
        final Long reviewLag = computeReviewLag(lastCommitBeforeFirstReview, commentsOrderByDate, mergeDate,
                lastCommit);
        final Long reviewTime = computeReviewTime(commentsOrderByDate, mergeDate,
                lastCommit);
        final Long deployTime = computeDeployTime();
        return SingleLeadTime.builder()
                .codingTime(codingTime)
                .reviewLag(reviewLag)
                .reviewTime(reviewTime)
                .deployTime(deployTime)
                .value(codingTime + reviewLag + reviewTime)
                .build();
    }

    private static Long computeDeployTime() {
        return null;
    }

    private static Long computeReviewTime(final List<Comment> commentsOrderByDate,
                                          final Date mergeDate, final Commit lastCommit) {
        if (!commentsOrderByDate.isEmpty()) {
            final Comment lastComment = commentsOrderByDate.get(commentsOrderByDate.size() - 1);
            return getNumberOfMinutesBetweenDates(lastComment.getCreationDate(), mergeDate);
        } else {
            final Long minutesBetweenLastCommitAndMerge = getNumberOfMinutesBetweenDates(lastCommit.getDate(),
                    mergeDate);
            if (minutesBetweenLastCommitAndMerge <= getDefaultReviewTimeMinutes()) {
                return minutesBetweenLastCommitAndMerge;
            } else {
                return getDefaultReviewTimeMinutes();
            }
        }
    }

    private static Long computeReviewLag(final Commit lastCommitBeforeFirstReview,
                                         final List<Comment> commentsOrderByDate, final Date mergeDate,
                                         final Commit lastCommit) {
        if (!commentsOrderByDate.isEmpty()) {
            final Comment firstComment = commentsOrderByDate.get(0);
            return getNumberOfMinutesBetweenDates(lastCommitBeforeFirstReview.getDate(),
                    firstComment.getCreationDate());
        } else {
            final Long minutesBetweenLastCommitAndMerge = getNumberOfMinutesBetweenDates(lastCommit.getDate(),
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

    private static Long computeCodingTime(final Date pullRequestCreationDate,
                                          final Commit lastCommitBeforeFirstReview) {

        return getNumberOfMinutesBetweenDates(pullRequestCreationDate,
                lastCommitBeforeFirstReview.getDate());
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


    @Builder(toBuilder = true)
    @Value
    private static class SingleLeadTime {
        Long value;
        Long codingTime;
        Long reviewLag;
        Long reviewTime;
        Long deployTime;
    }
}
