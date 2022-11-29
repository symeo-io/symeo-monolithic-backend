package io.symeo.monolithic.backend.job.domain.vcs;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.vcs.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.getNumberOfMinutesBetweenDates;
import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDateTime;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CycleTimeDataFactoryTest {
    private final static Faker faker = new Faker();

    @Nested
    public class MergeOnPullRequestRegexSettings {

        @Test
        void should_compute_cycle_time_given_one_commit() {
            // Given
            final CycleTimeDataFactory CycleTimeDataFactory = new CycleTimeDataFactory();
            final Date commitDate = stringToDateTime("2022-01-01 13:00:00");
            final Commit onlyCommit = Commit.builder()
                    .sha(faker.pokemon().name())
                    .date(commitDate)
                    .build();
            final PullRequest pullRequestCycleTimeToCompute =
                    PullRequest.builder()
                            .commits(List.of(onlyCommit))
                            .number(faker.number().numberBetween(1,100))
                            .build();
            final List<PullRequest> pullRequestsMatchingDeliverySettings = List.of();
            final List<Commit> allCommits = List.of(
                    onlyCommit
            );


            // When
            final CycleTime cycleTime = CycleTimeDataFactory.computeCycleTimeForMergeOnPullRequestMatchingDeliverySettings(
                    pullRequestCycleTimeToCompute,
                    pullRequestsMatchingDeliverySettings,
                    allCommits
            );

            // Then
            assertThat(cycleTime.getCodingTime()).isNull();
            assertThat(cycleTime.getTimeToDeploy()).isNull();
            assertThat(cycleTime.getReviewTime()).isEqualTo(
                    getNumberOfMinutesBetweenDates(commitDate, new Date())
            );
            assertThat(cycleTime.getValue()).isEqualTo(cycleTime.getReviewTime());
        }

        @Test
        void should_compute_coding_time_given_two_commits() {
            // Given
            final CycleTimeDataFactory cycleTimeDataFactory = new CycleTimeDataFactory();
            final Commit commit1 = Commit.builder()
                    .sha(faker.pokemon().name())
                    .date(stringToDateTime("2022-01-05 10:15:13"))
                    .build();
            final Commit commit2 = Commit.builder()
                    .sha(faker.pokemon().location())
                    .date(stringToDateTime("2022-01-03 22:05:00"))
                    .build();
            final PullRequest pullRequestCycleTimeToCompute =
                    PullRequest.builder()
                            .commits(List.of(commit1, commit2))
                            .number(faker.number().numberBetween(1,100))
                            .build();
            final List<PullRequest> pullRequestsMatchingDeliverySettings = List.of();
            final List<Commit> allCommits = List.of(
                    commit1,
                    commit2
            );


            // When
            final CycleTime cycleTime = cycleTimeDataFactory.computeCycleTimeForMergeOnPullRequestMatchingDeliverySettings(
                    pullRequestCycleTimeToCompute,
                    pullRequestsMatchingDeliverySettings,
                    allCommits
            );

            // Then
            assertThat(cycleTime.getCodingTime()).isEqualTo(2170L);
            assertThat(cycleTime.getReviewTime()).isEqualTo(getNumberOfMinutesBetweenDates(
                    commit1.getDate(),
                    new Date()
            ));
            assertThat(cycleTime.getTimeToDeploy()).isNull();
            assertThat(cycleTime.getValue()).isEqualTo(cycleTime.getCodingTime() + cycleTime.getReviewTime());
        }


        @Test
        void should_compute_review_time_given_commits_and_comments_for_merged_pr() {
            // Given
            final CycleTimeDataFactory CycleTimeDataFactory = new CycleTimeDataFactory();
            final Commit commit1 = Commit.builder().sha(faker.pokemon().name())
                    .date(stringToDateTime("2022-01-03 22:00:00")).build();
            final Commit commit2 = Commit.builder().sha(faker.pokemon().location())
                    .date(stringToDateTime("2022-01-02 15:30:00")).build();
            final Date mergeDate = stringToDateTime("2022-01-05 16:32:00");
            final PullRequest pullRequest = PullRequest.builder()
                    .creationDate(stringToDateTime("2022-01-01 13:00:00"))
                    .number(faker.number().numberBetween(1,100))
                    .mergeDate(mergeDate)
                    .commits(List.of(commit1, commit2))
                    .comments(List.of(
                            Comment.builder().creationDate(stringToDateTime("2022-01-04 07:58:00")).build(),
                            Comment.builder().creationDate(stringToDateTime("2022-01-04 08:14:00")).build()
                    ))
                    .build();
            final List<Commit> allCommits = List.of(commit2, commit1);

            // When
            final CycleTime cycleTime =
                    CycleTimeDataFactory.computeCycleTimeForMergeOnPullRequestMatchingDeliverySettings(pullRequest, List.of(),
                            allCommits);

            // Then
            assertThat(cycleTime.getCodingTime()).isEqualTo(1830L);
            assertThat(cycleTime.getReviewTime()).isEqualTo(2552L);
            assertThat(cycleTime.getValue()).isEqualTo(4382L);
            assertThat(cycleTime.getDeployDate()).isNull();
        }

        @Test
        void should_compute_review_time_given_commits_and_comments_for_open_pr() {
            // Given
            final CycleTimeDataFactory CycleTimeDataFactory = new CycleTimeDataFactory();
            final Commit commit1 = Commit.builder().sha(faker.pokemon().name())
                    .date(stringToDateTime("2022-01-03 22:00:00")).build();
            final Commit commit2 = Commit.builder().sha(faker.pokemon().location())
                    .date(stringToDateTime("2022-01-02 15:30:00")).build();
            final PullRequest pullRequest = PullRequest.builder()
                    .creationDate(stringToDateTime("2022-01-01 13:00:00"))
                    .number(faker.number().numberBetween(1,100))
                    .mergeDate(null)
                    .commits(List.of(commit1, commit2))
                    .comments(List.of(
                            Comment.builder().creationDate(stringToDateTime("2022-01-04 07:58:00")).build(),
                            Comment.builder().creationDate(stringToDateTime("2022-01-05 08:14:00")).build()
                    ))
                    .build();
            final List<Commit> allCommits = List.of(commit2, commit1);

            // When
            final CycleTime cycleTime =
                    CycleTimeDataFactory.computeCycleTimeForMergeOnPullRequestMatchingDeliverySettings(pullRequest, List.of(),
                            allCommits);

            // Then
            assertThat(cycleTime.getCodingTime()).isEqualTo(1830L);
            assertThat(cycleTime.getReviewTime())
                    .isEqualTo(
                            getNumberOfMinutesBetweenDates(commit1.getDate(),
                                    new Date()));
            assertThat(cycleTime.getTimeToDeploy()).isNull();
            assertThat(cycleTime.getDeployDate()).isNull();
        }


        @Test
        void should_compute_zero_review_lag_and_time_given_commits_and_empty_comments() {
            // Given
            final CycleTimeDataFactory CycleTimeDataFactory = new CycleTimeDataFactory();
            final Commit commit = Commit.builder().sha(faker.pokemon().name()).date(stringToDateTime("2022-01" +
                    "-03 " +
                    "15:30:00")).build();
            final PullRequest pullRequest = PullRequest.builder()
                    .creationDate(stringToDateTime("2022-01-01 13:00:00"))
                    .number(faker.number().numberBetween(1,100))
                    .mergeDate(stringToDateTime("2022-01-03 15:55:00"))
                    .commits(List.of(commit))
                    .build();

            // When
            final CycleTime cycleTime =
                    CycleTimeDataFactory.computeCycleTimeForMergeOnPullRequestMatchingDeliverySettings(pullRequest, List.of(),
                            List.of(commit));

            // Then
            assertThat(cycleTime.getReviewTime()).isEqualTo(25L);
            assertThat(cycleTime.getDeployDate()).isNull();
        }

        @Test
        void should_compute_review_time_given_a_single_commit() {
            // Given
            final CycleTimeDataFactory CycleTimeDataFactory = new CycleTimeDataFactory();
            final Commit commit = Commit.builder().sha(faker.pokemon().name())
                    .date(stringToDateTime("2022-01-03 15:55:00")).build();
            final PullRequest pullRequest = PullRequest.builder()
                    .creationDate(stringToDateTime("2022-01-01 13:00:00"))
                    .number(faker.number().numberBetween(1,100))
                    .mergeDate(stringToDateTime("2022-01-03 17:30:00"))
                    .commits(List.of(commit))
                    .build();

            // When
            final CycleTime cycleTime =
                    CycleTimeDataFactory.computeCycleTimeForMergeOnPullRequestMatchingDeliverySettings(pullRequest, List.of(),
                            List.of(commit));

            // Then
            assertThat(cycleTime.getCodingTime()).isNull();
            assertThat(cycleTime.getReviewTime()).isEqualTo(95L);
            assertThat(cycleTime.getTimeToDeploy()).isNull();
            assertThat(cycleTime.getValue()).isEqualTo(95L);
            assertThat(cycleTime.getDeployDate()).isNull();
        }


        @Test
        void should_compute_deploy_time_for_simple_use_case() throws SymeoException {
            // Given
            final CycleTimeDataFactory CycleTimeDataFactory = new CycleTimeDataFactory();
            final Commit commit1 = Commit.builder().sha(faker.pokemon().name() + "-1").date(stringToDateTime(
                    "2022-01" +
                            "-03 15:55:00")).build();
            final Commit commit0 = Commit.builder().sha(faker.pokemon().name() + "-0").date(stringToDateTime(
                    "2022-01" +
                            "-02 15:55:00")).build();
            final Commit mergeCommit = Commit.builder()
                    .sha(faker.pokemon().name() + "-2")
                    .date(stringToDateTime("2022-01-04 16:00:00"))
                    .parentShaList(List.of(commit1.getSha(), commit0.getSha()))
                    .build();
            final Commit commit21 = Commit.builder()
                    .sha(faker.pokemon().name() + "-21")
                    .date(stringToDateTime("2022-01-05 14:00:00"))
                    .parentShaList(List.of(mergeCommit.getSha()))
                    .build();
            final Commit commit20 = Commit.builder()
                    .sha(faker.pokemon().name() + "-20")
                    .date(stringToDateTime("2022-01-05 14:00:00"))
                    .build();
            final Commit mergeCommit2 = Commit.builder()
                    .sha(faker.pokemon().name() + "-2-merge")
                    .date(stringToDateTime("2022-01-05 14:00:00"))
                    .parentShaList(List.of(commit21.getSha(), commit20.getSha()))
                    .build();
            final Commit commit30 = Commit.builder()
                    .sha(faker.pokemon().name() + "-30")
                    .date(stringToDateTime("2022-01-05 14:00:00"))
                    .build();
            final Commit commit31 = Commit.builder()
                    .sha(faker.pokemon().name() + "-31")
                    .date(stringToDateTime("2022-01-05 14:00:00"))
                    .parentShaList(List.of(commit30.getSha(), mergeCommit2.getSha()))
                    .build();
            final Commit commit3Merge = Commit.builder()
                    .sha(faker.pokemon().name() + "-3-merge")
                    .date(stringToDateTime("2022-01-10 10:00:00"))
                    .parentShaList(List.of(commit31.getSha()))
                    .build();


            final PullRequest pullRequest = PullRequest.builder()
                    .creationDate(stringToDateTime("2022-01-01 13:00:00"))
                    .number(faker.number().numberBetween(1,100))
                    .mergeDate(mergeCommit.getDate())
                    .mergeCommitSha(mergeCommit.getSha())
                    .commits(List.of(mergeCommit))
                    .build();

            final List<PullRequest> pullRequestsMatchingDeliverySettings = List.of(
                    PullRequest.builder()
                            .creationDate(stringToDateTime("2022-01-01 13:00:00"))
                            .number(faker.number().numberBetween(1,100))
                            .mergeDate(mergeCommit2.getDate())
                            .mergeCommitSha(mergeCommit2.getSha())
                            .commits(List.of(mergeCommit2))
                            .build(),
                    PullRequest.builder()
                            .creationDate(stringToDateTime("2022-01-01 13:00:00"))
                            .number(faker.number().numberBetween(1,100))
                            .mergeDate(commit3Merge.getDate())
                            .mergeCommitSha(commit3Merge.getSha())
                            .commits(List.of(commit3Merge))
                            .build()
            );

            // When
            final CycleTime cycleTime =
                    CycleTimeDataFactory.computeCycleTimeForMergeOnPullRequestMatchingDeliverySettings(pullRequest,
                            pullRequestsMatchingDeliverySettings,
                            List.of(commit0, commit1, mergeCommit, commit20, commit21, mergeCommit2, commit30,
                                    commit31, commit3Merge));

            // Then
            assertThat(cycleTime.getCodingTime()).isNull();
            assertThat(cycleTime.getReviewTime()).isEqualTo(0L);
            assertThat(cycleTime.getTimeToDeploy()).isEqualTo(1320L);
            assertThat(cycleTime.getValue()).isEqualTo(1320L);
            assertThat(cycleTime.getDeployDate()).isEqualTo(stringToDateTime("2022-01-05 14:00:00"));
        }


        @Test
        void should_manage_pull_request_with_branch_deleted_on_vcs() {
            // Given
            final CycleTimeDataFactory CycleTimeDataFactory = new CycleTimeDataFactory();
            final Commit commit1 = Commit.builder().sha(faker.pokemon().name() + "-1").date(stringToDateTime(
                    "2022-01" +
                            "-03 15:55:00")).build();
            final Commit mergeCommit = Commit.builder()
                    .sha(faker.pokemon().name() + "-2")
                    .date(stringToDateTime("2022-01-04 16:00:00"))
                    .parentShaList(List.of(commit1.getSha()))
                    .build();
            final Commit mergeCommit2 = Commit.builder()
                    .sha(faker.pokemon().name() + "-2-merge")
                    .date(stringToDateTime("2022-01-05 14:00:00"))
                    .parentShaList(List.of())
                    .build();

            final PullRequest pullRequest = PullRequest.builder()
                    .creationDate(stringToDateTime("2022-01-01 13:00:00"))
                    .number(faker.number().numberBetween(1,100))
                    .mergeDate(mergeCommit.getDate())
                    .mergeCommitSha(mergeCommit.getSha())
                    .commitShaList(List.of(mergeCommit.getSha()))
                    .build();

            // When
            CycleTimeDataFactory.computeCycleTimeForMergeOnPullRequestMatchingDeliverySettings(
                    pullRequest,
                    List.of(PullRequest.builder()
                            .creationDate(stringToDateTime("2022-01-01 13:00:00"))
                            .number(faker.number().numberBetween(1,100))
                            .mergeDate(mergeCommit2.getDate())
                            .mergeCommitSha(mergeCommit2.getSha())
                            .commitShaList(List.of(mergeCommit2.getSha()))
                            .build()),
                    List.of());

            // Then
            assertThat(true);
        }
    }

    @Nested
    public class TagToDeployMatchingRegexSettings {

        @Test
        void should_compute_deploy_time_for_simple_use_case() {
            // Given
            final CycleTimeDataFactory CycleTimeDataFactory = new CycleTimeDataFactory();
            final Commit commit1 = Commit.builder().sha(faker.pokemon().name() + "-1").date(stringToDateTime(
                    "2022-01" +
                            "-03 15:55:00")).build();
            final Commit commit0 = Commit.builder().sha(faker.pokemon().name() + "-0").date(stringToDateTime(
                    "2022-01" +
                            "-02 15:55:00")).build();
            final Commit mergeCommit = Commit.builder()
                    .sha(faker.pokemon().name() + "-2")
                    .date(stringToDateTime("2022-01-04 16:00:00"))
                    .parentShaList(List.of(commit1.getSha(), commit0.getSha()))
                    .build();
            final Commit commit21 = Commit.builder()
                    .sha(faker.pokemon().name() + "-21")
                    .date(stringToDateTime("2022-01-05 14:00:00"))
                    .parentShaList(List.of(mergeCommit.getSha()))
                    .build();
            final Commit commit20 = Commit.builder()
                    .sha(faker.pokemon().name() + "-20")
                    .date(stringToDateTime("2022-01-05 14:00:00"))
                    .build();
            final Commit mergeCommit2 = Commit.builder()
                    .sha(faker.pokemon().name() + "-2-merge")
                    .date(stringToDateTime("2022-01-05 14:00:00"))
                    .parentShaList(List.of(commit21.getSha(), commit20.getSha()))
                    .build();
            final Commit commit30 = Commit.builder()
                    .sha(faker.pokemon().name() + "-30")
                    .date(stringToDateTime("2022-01-05 14:00:00"))
                    .build();
            final Commit commit31 = Commit.builder()
                    .sha(faker.pokemon().name() + "-31")
                    .date(stringToDateTime("2022-01-05 14:00:00"))
                    .parentShaList(List.of(commit30.getSha(), mergeCommit2.getSha()))
                    .build();
            final Commit commit3Merge = Commit.builder()
                    .sha(faker.pokemon().name() + "-3-merge")
                    .date(stringToDateTime("2022-01-10 10:00:00"))
                    .parentShaList(List.of(commit31.getSha()))
                    .build();

            final PullRequest pullRequest = PullRequest.builder()
                    .creationDate(stringToDateTime("2022-01-01 13:00:00"))
                    .number(faker.number().numberBetween(1,100))
                    .mergeDate(mergeCommit.getDate())
                    .mergeCommitSha(mergeCommit.getSha())
                    .commits(List.of(mergeCommit))
                    .commitShaList(List.of())
                    .build();

            final List<Tag> tags = List.of(
                    Tag.builder().name(faker.name().firstName()).commitSha(mergeCommit2.getSha()).build(),
                    Tag.builder().name(faker.name().lastName()).commitSha(commit3Merge.getSha()).build()
            );

            // When
            final CycleTime cycleTime =
                    CycleTimeDataFactory.computeCycleTimeForTagRegexToDeploySettings(pullRequest,
                            tags,
                            List.of(commit0, commit1, mergeCommit, commit20, commit21, mergeCommit2, commit30,
                                    commit31, commit3Merge));

            // Then
//            "2022-01-04 16:00:00"
//            "2022-01-05 14:00:00"
            assertThat(cycleTime.getCodingTime()).isNull();
            assertThat(cycleTime.getReviewTime()).isEqualTo(0L);
            assertThat(cycleTime.getTimeToDeploy()).isEqualTo(1320L);
            assertThat(cycleTime.getValue()).isEqualTo(1320L);
            assertThat(cycleTime.getDeployDate()).isEqualTo(stringToDateTime("2022-01-05 14:00:00"));
        }
    }

}