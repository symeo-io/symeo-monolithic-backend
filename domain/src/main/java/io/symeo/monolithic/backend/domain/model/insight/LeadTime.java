package io.symeo.monolithic.backend.domain.model.insight;

import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.Comparator;
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
        final List<Commit> commits = new ArrayList<>(pullRequestView.getCommits());
        commits.sort(Comparator.comparing(Commit::getDate));
        final Float codingTime = getNumberOfDaysWithOneDecimalBetweenDates(pullRequestView.getCreationDate(),
                commits.get(commits.size() - 1).getDate());
        return LeadTime.builder()
                .averageCodingTime(codingTime)
                .build();
    }
}
