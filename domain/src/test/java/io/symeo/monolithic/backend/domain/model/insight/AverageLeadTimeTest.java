package io.symeo.monolithic.backend.domain.model.insight;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Comment;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDateTime;
import static io.symeo.monolithic.backend.domain.model.insight.AverageLeadTime.buildForPullRequestMergedOnBranchRegexSettings;
import static org.assertj.core.api.Assertions.assertThat;

public class AverageLeadTimeTest {

//    @Nested
    public class CodingTimeFeatures {

        @Test
        void should_compute_coding_time_given_one_commit() throws SymeoException {
            // Given
            final List<PullRequestView> pullRequestViews = List.of(
                    PullRequestView.builder()
                            .status(PullRequest.MERGE)
                            .mergeDate(stringToDateTime("2022-01-03 13:00:00"))
                            .commits(
                                    List.of(Commit.builder().date(stringToDateTime("2022-01-03 22:05:00")).build())
                            ).build()
            );

            // When
            final AverageLeadTime averageLeadTime =
                    buildForPullRequestMergedOnBranchRegexSettings(pullRequestViews, List.of(), List.of()).get();

            // Then
            assertThat(averageLeadTime.getAverageCodingTime()).isEqualTo(0f);
        }

        @Test
        void should_compute_coding_time_given_several_commits() throws SymeoException {
            // Given
            final List<PullRequestView> pullRequestViews = List.of(
                    PullRequestView.builder()
                            .status(PullRequest.MERGE)
                            .mergeDate(stringToDateTime("2022-01-03 13:00:00"))
                            .commits(
                                    List.of(Commit.builder().date(stringToDateTime("2022-01-03 07:30:00")).build(),
                                            Commit.builder().date(stringToDateTime("2022-01-04 22:00:00")).build(),
                                            Commit.builder().date(stringToDateTime("2022-01-07 10:00:00")).build())
                            ).build()
            );

            // When
            final AverageLeadTime averageLeadTime = buildForPullRequestMergedOnBranchRegexSettings(pullRequestViews,
                    List.of(), List.of()).get();

            // Then
            assertThat(averageLeadTime.getAverageCodingTime()).isEqualTo(5910.0f);
        }

    }

//    @Nested
    public class ReviewLagAndTimeFeatures {

        @Test
        void should_compute_review_lag_and_time_given_commits_and_comments() throws SymeoException {
            // Given
            final List<PullRequestView> pullRequestViews = List.of(
                    PullRequestView.builder()
                            .status(PullRequest.MERGE)
                            .creationDate(stringToDateTime("2022-01-01 13:00:00"))
                            .mergeDate(stringToDateTime("2022-01-05 16:32:00"))
                            .commits(
                                    List.of(
                                            Commit.builder().date(stringToDateTime("2022-01-03 22:00:00")).build(),
                                            Commit.builder().date(stringToDateTime("2022-01-02 15:30:00")).build())
                            )
                            .comments(List.of(
                                    Comment.builder().creationDate(stringToDateTime("2022-01-04 07:58:00")).build(),
                                    Comment.builder().creationDate(stringToDateTime("2022-01-04 08:14:00")).build()
                            ))
                            .build()
            );

            // When
            final AverageLeadTime averageLeadTime = buildForPullRequestMergedOnBranchRegexSettings(pullRequestViews,
                    List.of(), List.of()).get();

            // Then
            assertThat(averageLeadTime.getAverageReviewLag()).isEqualTo(598.0f);
            assertThat(averageLeadTime.getAverageReviewTime()).isEqualTo(1938.0f);
        }

        @Test
        void should_compute_zero_review_lag_and_time_given_commits_and_empty_commits() {
            // Given
            final List<PullRequestView> pullRequestViews = List.of(
                    PullRequestView.builder()
                            .status(PullRequest.MERGE)
                            .creationDate(stringToDateTime("2022-01-01 13:00:00"))
                            .mergeDate(stringToDateTime("2022-01-03 15:55:00"))
                            .commits(
                                    List.of(Commit.builder().date(stringToDateTime("2022-01-03 15:30:00")).build())
                            )
                            .build()
            );

            // When
            final AverageLeadTime averageLeadTime = buildForPullRequestMergedOnBranchRegexSettings(pullRequestViews,
                    List.of(), List.of()).get();

            // Then
            assertThat(averageLeadTime.getAverageReviewLag()).isEqualTo(0.0f);
            assertThat(averageLeadTime.getAverageReviewTime()).isEqualTo(25.0f);
        }

        @Test
        void should_compute_review_lag_and_time_with_default_value_given_commits_and_empty_commits() {
            // Given
            final List<PullRequestView> pullRequestViews = List.of(
                    PullRequestView.builder()
                            .status(PullRequest.MERGE)
                            .creationDate(stringToDateTime("2022-01-01 13:00:00"))
                            .mergeDate(stringToDateTime("2022-01-03 17:30:00"))
                            .commits(
                                    List.of(Commit.builder().date(stringToDateTime("2022-01-03 15:55:00")).build())
                            )
                            .build()
            );

            // When
            final AverageLeadTime averageLeadTime = buildForPullRequestMergedOnBranchRegexSettings(pullRequestViews,
                    List.of(), List.of()).get();

            // Then
            assertThat(averageLeadTime.getAverageReviewLag()).isEqualTo(35.0f);
            assertThat(averageLeadTime.getAverageReviewTime()).isEqualTo(60.0f);
        }

    }

//    @Nested
    public class EdgeCases {
        @Test
        void should_compute_lead_time_value() {
            // Given
            final List<PullRequestView> pullRequestViews = List.of(
                    PullRequestView.builder()
                            .status(PullRequest.MERGE)
                            .creationDate(stringToDateTime("2022-01-01 13:00:00"))
                            .mergeDate(stringToDateTime("2022-01-05 16:32:00"))
                            .commits(
                                    List.of(
                                            Commit.builder().date(stringToDateTime("2022-01-03 22:00:00")).build(),
                                            Commit.builder().date(stringToDateTime("2022-01-02 15:30:00")).build())
                            )
                            .comments(List.of(
                                    Comment.builder().creationDate(stringToDateTime("2022-01-04 07:58:00")).build(),
                                    Comment.builder().creationDate(stringToDateTime("2022-01-04 08:14:00")).build()
                            ))
                            .build()
            );

            // When
            final AverageLeadTime averageLeadTime = buildForPullRequestMergedOnBranchRegexSettings(pullRequestViews,
                    List.of(), List.of()).get();

            // Then
            assertThat(averageLeadTime.getAverageCodingTime()).isEqualTo(1830.0f);
            assertThat(averageLeadTime.getAverageReviewLag()).isEqualTo(598.0f);
            assertThat(averageLeadTime.getAverageReviewTime()).isEqualTo(1938.0f);
            assertThat(averageLeadTime.getAverageValue()).isEqualTo(4366.0f);
        }

        @Test
        void should_return_null_if_pull_requests_are_empty() {
            // Given
            final List<PullRequestView> pullRequestViews = List.of();

            // When
            final Optional<AverageLeadTime> leadTime =
                    buildForPullRequestMergedOnBranchRegexSettings(pullRequestViews, List.of(), List.of());

            // Then
            assertThat(leadTime.isEmpty()).isTrue();

        }

        @Test
        void should_filter_pull_requests_without_commits() {
            // Given
            final List<PullRequestView> pullRequestViews = List.of(
                    PullRequestView.builder()
                            .status(PullRequest.MERGE)
                            .creationDate(stringToDateTime("2022-01-01 13:00:00"))
                            .mergeDate(stringToDateTime("2022-01-03 15:55:00"))
                            .commits(
                                    List.of(Commit.builder().date(stringToDateTime("2022-01-03 15:30:00")).build())
                            )
                            .build(),
                    PullRequestView.builder().build()
            );

            // When
            final AverageLeadTime averageLeadTime = buildForPullRequestMergedOnBranchRegexSettings(pullRequestViews,
                    List.of(), List.of()).get();

            // Then
            assertThat(averageLeadTime).isNotNull();
            assertThat(averageLeadTime.getAverageReviewLag()).isEqualTo(0.0f);
            assertThat(averageLeadTime.getAverageReviewTime()).isEqualTo(25.0f);
        }

        @Test
        void should_compute_average_lead_time_given_list_of_pull_requests() {
            // Given
            final List<PullRequestView> pullRequestViews = List.of(
                    PullRequestView.builder()
                            .status(PullRequest.MERGE)
                            .creationDate(stringToDateTime("2022-01-01 13:00:00"))
                            .mergeDate(stringToDateTime("2022-01-05 16:32:00"))
                            .commits(
                                    List.of(
                                            Commit.builder().date(stringToDateTime("2022-01-03 22:00:00")).build(),
                                            Commit.builder().date(stringToDateTime("2022-01-02 15:30:00")).build())
                            )
                            .comments(List.of(
                                    Comment.builder().creationDate(stringToDateTime("2022-01-04 07:58:00")).build(),
                                    Comment.builder().creationDate(stringToDateTime("2022-01-04 08:14:00")).build()
                            ))
                            .build(),
                    PullRequestView.builder()
                            .status(PullRequest.MERGE)
                            .creationDate(stringToDateTime("2022-01-01 13:00:00"))
                            .mergeDate(stringToDateTime("2022-01-03 17:30:00"))
                            .commits(
                                    List.of(Commit.builder().date(stringToDateTime("2022-01-03 15:55:00")).build())
                            ).build()
            );

            // When
            final AverageLeadTime averageLeadTime = buildForPullRequestMergedOnBranchRegexSettings(pullRequestViews,
                    List.of(), List.of()).get();

            // Then
            assertThat(averageLeadTime.getAverageCodingTime()).isEqualTo(915.0f);
            assertThat(averageLeadTime.getAverageReviewLag()).isEqualTo(316.5f);
            assertThat(averageLeadTime.getAverageReviewTime()).isEqualTo(999.0f);
        }
    }

}
