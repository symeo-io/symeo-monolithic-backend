package io.symeo.monolithic.backend.domain.domain.insight;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.insight.LeadTime;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDateTime;
import static org.assertj.core.api.Assertions.assertThat;

public class LeadTimeTest {

    @Test
    void should_compute_coding_time_given_one_commit() throws SymeoException {
        // Given
        final List<PullRequestView> pullRequestViews = List.of(
                PullRequestView.builder()
                        .creationDate(stringToDateTime("2022-01-01 12:00:00"))
                        .commits(
                                List.of(Commit.builder().date(stringToDateTime("2022-01-03 22:00:00")).build())
                        ).build()
        );

        // When
        final LeadTime leadTime = LeadTime.buildFromPullRequestWithCommitsViews(pullRequestViews);

        // Then
        assertThat(leadTime.getAverageCodingTime()).isEqualTo(2.9f);
    }

    @Test
    void should_compute_coding_time_given_several_commits() throws SymeoException {
        // Given
        final List<PullRequestView> pullRequestViews = List.of(
                PullRequestView.builder()
                        .creationDate(stringToDateTime("2022-01-01 12:00:00"))
                        .commits(
                                List.of(Commit.builder().date(stringToDateTime("2022-01-03 22:00:00")).build(),
                                        Commit.builder().date(stringToDateTime("2022-01-04 22:00:00")).build(),
                                        Commit.builder().date(stringToDateTime("2022-01-07 10:00:00")).build())
                        ).build()
        );

        // When
        final LeadTime leadTime = LeadTime.buildFromPullRequestWithCommitsViews(pullRequestViews);

        // Then
        assertThat(leadTime.getAverageCodingTime()).isEqualTo(6.4f);
    }


}
