package io.symeo.monolithic.backend.domain.model.insight;

import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Comment;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.getNumberOfDaysWithOneDecimalBetweenDates;

@Value
@Builder
public class LeadTime {
    Float average;
    Float averageCodingTime;
    Float averageReviewLage;
    Float averageReviewTime;
    Float averageDeployTime;

    public static LeadTime buildFromPullRequestWithCommitsViews(final List<PullRequestView> pullRequestWithCommitsViews) {
        return computeLeadTimeForPullRequestView(pullRequestWithCommitsViews.get(0));
    }

    private static LeadTime computeLeadTimeForPullRequestView(final PullRequestView pullRequestView) {
        final List<Commit> commits = pullRequestView.getCommitsOrderByDate();
        final List<Comment> comments = pullRequestView.getCommentsOrderByDate();
        final Float codingTime = computeCodingTime(pullRequestView, commits, comments);
        return LeadTime.builder()
                .averageCodingTime(codingTime)
                .build();
    }

    private static Float computeCodingTime(PullRequestView pullRequestView, List<Commit> commits,
                                           List<Comment> comments) {
        Commit lastCommitBeforeFirstReview;
        if (!comments.isEmpty()) {
            final Comment firstComment = comments.get(0);
            if (firstComment.getCreationDate().before(commits.get(0).getDate())) {
                lastCommitBeforeFirstReview = commits.get(0);
            } else {
                lastCommitBeforeFirstReview = commits.get(0);
                for (Commit commit : commits) {
                    if (commit.getDate().before(firstComment.getCreationDate())) {
                        lastCommitBeforeFirstReview = commit;
                    }
                }
            }
        } else {
            lastCommitBeforeFirstReview = commits.get(commits.size() - 1);
        }
        return getNumberOfDaysWithOneDecimalBetweenDates(pullRequestView.getCreationDate(),
                lastCommitBeforeFirstReview.getDate());
    }
}
