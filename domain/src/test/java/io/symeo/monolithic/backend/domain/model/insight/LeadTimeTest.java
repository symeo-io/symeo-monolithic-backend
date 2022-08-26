package io.symeo.monolithic.backend.domain.model.insight;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Comment;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDateTime;
import static org.assertj.core.api.Assertions.assertThat;

public class LeadTimeTest {

    @Nested
    public class CodingTimeFeatures {

        @Test
        void should_compute_coding_time_given_one_commit() throws SymeoException {
            // Given
            final List<PullRequestView> pullRequestViews = List.of(
                    PullRequestView.builder()
                            .creationDate(stringToDateTime("2022-01-01 13:00:00"))
                            .commits(
                                    List.of(Commit.builder().date(stringToDateTime("2022-01-03 22:00:00")).build())
                            ).build()
            );

            // When
            final LeadTime leadTime = LeadTime.buildFromPullRequestWithCommitsViews(pullRequestViews);

            // Then
            assertThat(leadTime.getAverageCodingTime()).isEqualTo(2.4f);
        }

        @Test
        void should_compute_coding_time_given_several_commits() throws SymeoException {
            // Given
            final List<PullRequestView> pullRequestViews = List.of(
                    PullRequestView.builder()
                            .creationDate(stringToDateTime("2022-01-01 13:00:00"))
                            .commits(
                                    List.of(Commit.builder().date(stringToDateTime("2022-01-03 22:00:00")).build(),
                                            Commit.builder().date(stringToDateTime("2022-01-04 22:00:00")).build(),
                                            Commit.builder().date(stringToDateTime("2022-01-07 10:00:00")).build())
                            ).build()
            );

            // When
            final LeadTime leadTime = LeadTime.buildFromPullRequestWithCommitsViews(pullRequestViews);

            // Then
            assertThat(leadTime.getAverageCodingTime()).isEqualTo(5.9f);
        }

        @Test
        void should_compute_coding_time_given_several_commits_and_pull_request_comments() {
            // Given
            final List<PullRequestView> pullRequestViews = List.of(
                    PullRequestView.builder()
                            .creationDate(stringToDateTime("2022-01-01 13:00:00"))
                            .comments(
                                    List.of(
                                            Comment.builder().creationDate(stringToDateTime("2022-01-04 15:00:00")).build(),
                                            Comment.builder().creationDate(stringToDateTime("2022-01-05 22:00:00")).build()
                                    )
                            )
                            .commits(
                                    List.of(Commit.builder().date(stringToDateTime("2022-01-03 22:00:00")).build(),
                                            Commit.builder().date(stringToDateTime("2022-01-04 22:00:00")).build(),
                                            Commit.builder().date(stringToDateTime("2022-01-07 10:00:00")).build())
                            ).build()
            );

            final LeadTime leadTime = LeadTime.buildFromPullRequestWithCommitsViews(pullRequestViews);

            // Then
            assertThat(leadTime.getAverageCodingTime()).isEqualTo(2.4f);
        }

        @Test
        void should_compute_coding_time_given_a_first_comment_before_first_commit() {
            // Given
            final List<PullRequestView> pullRequestViews = List.of(
                    PullRequestView.builder()
                            .creationDate(stringToDateTime("2022-01-01 13:00:00"))
                            .comments(
                                    List.of(
                                            Comment.builder().creationDate(stringToDateTime("2022-01-01 10:00:00")).build(),
                                            Comment.builder().creationDate(stringToDateTime("2022-01-05 22:00:00")).build()
                                    )
                            )
                            .commits(
                                    List.of(Commit.builder().date(stringToDateTime("2022-01-03 22:00:00")).build(),
                                            Commit.builder().date(stringToDateTime("2022-01-04 22:00:00")).build(),
                                            Commit.builder().date(stringToDateTime("2022-01-07 10:00:00")).build())
                            ).build()
            );

            final LeadTime leadTime = LeadTime.buildFromPullRequestWithCommitsViews(pullRequestViews);

            // Then
            assertThat(leadTime.getAverageCodingTime()).isEqualTo(2.4f);
        }
    }


    @Test
    void should_filter_pull_requests_without_commits() {
        // Given

        // When

        // Then
    }
}
