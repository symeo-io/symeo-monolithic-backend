package domain.model.insight;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.bff.model.metric.AverageCycleTime;
import io.symeo.monolithic.backend.domain.bff.model.vcs.CommitView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.TagView;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.getNumberOfMinutesBetweenDates;
import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDateTime;
import static org.assertj.core.api.Assertions.assertThat;

public class AverageCycleTimeTest {

    private static final Faker faker = new Faker();

    @Test
    void should_compute_average_cycle_given_one_commit_on_open_pr() {
        // Given
        final CommitView commit = CommitView.builder().sha(faker.pokemon().name())
                .date(stringToDateTime("2022-01-01 12:00:00"))
                .build();

        // When
        final AverageCycleTime averageCycleTime =
                AverageCycleTime.computeCycleTimeForPullRequestMergedOnBranchRegexSettings(
                        List.of(
                                PullRequestView.builder().commitShaList(List.of(commit.getSha())).build()
                        ),
                        List.of(),
                        List.of(commit)
                );

        // Then
        assertThat(averageCycleTime.getAverageCodingTime()).isNull();
        assertThat(averageCycleTime.getAverageDeployTime()).isNull();
        assertThat(averageCycleTime.getAverageReviewTime())
                .isEqualTo(getNumberOfMinutesBetweenDates(commit.getDate(), new Date()) * 1f);
        assertThat(averageCycleTime.getAverageValue())
                .isEqualTo(getNumberOfMinutesBetweenDates(commit.getDate(), new Date()) * 1f);
    }


    @Test
    void should_compute_average_cycle_time_given_two_commits_on_merge_pr() {
        // Given
        final CommitView commit1 = CommitView.builder().sha(faker.pokemon().name())
                .date(stringToDateTime("2022-01-01 12:00:00"))
                .build();
        final CommitView commit2 = CommitView.builder().sha(faker.pokemon().location())
                .date(stringToDateTime("2022-01-02 12:00:00"))
                .build();


        // When
        final AverageCycleTime averageCycleTime =
                AverageCycleTime.computeCycleTimeForPullRequestMergedOnBranchRegexSettings(
                        List.of(
                                PullRequestView.builder().commitShaList(List.of(commit1.getSha(), commit2.getSha())).build()
                        ),
                        List.of(),
                        List.of(commit1, commit2)
                );

        // Then
        assertThat(averageCycleTime.getAverageCodingTime()).isEqualTo(1440f);
        assertThat(averageCycleTime.getAverageDeployTime()).isNull();
        assertThat(averageCycleTime.getAverageReviewTime())
                .isEqualTo(getNumberOfMinutesBetweenDates(commit2.getDate(), new Date()) * 1f);
        assertThat(averageCycleTime.getAverageValue())
                .isEqualTo(getNumberOfMinutesBetweenDates(commit2.getDate(), new Date()) * 1f + 1440f);
    }

    @Test
    void should_compute_average_cycle_time_given_two_commits_on_a_merged_pr() {
        // Given
        final CommitView commit1 = CommitView.builder().sha(faker.pokemon().name())
                .date(stringToDateTime("2022-01-01 12:00:00"))
                .build();
        final CommitView commit2 = CommitView.builder().sha(faker.pokemon().location())
                .date(stringToDateTime("2022-01-02 12:00:00"))
                .build();
        final CommitView mergeCommit = CommitView.builder().sha(faker.pokemon().location())
                .date(stringToDateTime("2022-01-02 14:00:00"))
                .build();


        // When
        final AverageCycleTime averageCycleTime =
                AverageCycleTime.computeCycleTimeForPullRequestMergedOnBranchRegexSettings(
                        List.of(
                                PullRequestView.builder()
                                        .commitShaList(
                                                List.of(commit1.getSha(), commit2.getSha())
                                        )
                                        .mergeDate(mergeCommit.getDate())
                                        .build()
                        ),
                        List.of(),
                        List.of(commit1, commit2, mergeCommit)
                );

        // Then
        assertThat(averageCycleTime.getAverageCodingTime()).isEqualTo(1440f);
        assertThat(averageCycleTime.getAverageDeployTime()).isNull();
        assertThat(averageCycleTime.getAverageReviewTime())
                .isEqualTo(getNumberOfMinutesBetweenDates(commit2.getDate(), mergeCommit.getDate()) * 1f);
        assertThat(averageCycleTime.getAverageValue())
                .isEqualTo(getNumberOfMinutesBetweenDates(commit2.getDate(), mergeCommit.getDate()) * 1f + 1440f);
    }

    @Test
    void should_compute_average_cycle_time_given_two_commits_on_a_merged_pr_deploy_on_merge_pr_regex() {
        // Given
        final CommitView commit1 = CommitView.builder().sha(faker.pokemon().name())
                .date(stringToDateTime("2022-01-01 12:00:00"))
                .build();
        final CommitView commit2 = CommitView.builder().sha(faker.pokemon().location())
                .date(stringToDateTime("2022-01-02 12:00:00"))
                .build();
        final CommitView mergeCommit = CommitView.builder().sha(faker.rickAndMorty().location())
                .date(stringToDateTime("2022-01-02 14:00:00"))
                .parentShaList(List.of(commit2.getSha()))
                .build();
        final CommitView mergeCommit2 = CommitView.builder().sha(faker.rickAndMorty().character())
                .date(stringToDateTime("2022-01-03 16:00:00"))
                .parentShaList(List.of(mergeCommit.getSha()))
                .build();


        // When
        final AverageCycleTime averageCycleTime =
                AverageCycleTime.computeCycleTimeForPullRequestMergedOnBranchRegexSettings(
                        List.of(
                                PullRequestView.builder()
                                        .commitShaList(
                                                List.of(commit1.getSha(), commit2.getSha())
                                        )
                                        .mergeCommitSha(mergeCommit.getSha())
                                        .mergeDate(mergeCommit.getDate())
                                        .build()
                        ),
                        List.of(PullRequestView.builder()
                                .mergeDate(mergeCommit2.getDate())
                                .mergeCommitSha(mergeCommit2.getSha())
                                .commits(List.of(mergeCommit))
                                .build()),
                        List.of(commit1, commit2, mergeCommit, mergeCommit2)
                );

        // Then
        assertThat(averageCycleTime.getAverageCodingTime()).isEqualTo(1440f);
        assertThat(averageCycleTime.getAverageReviewTime())
                .isEqualTo(getNumberOfMinutesBetweenDates(commit2.getDate(), mergeCommit.getDate()) * 1f);
        assertThat(averageCycleTime.getAverageDeployTime())
                .isEqualTo(getNumberOfMinutesBetweenDates(mergeCommit.getDate(), mergeCommit2.getDate()) * 1f);
        assertThat(averageCycleTime.getAverageValue())
                .isEqualTo(getNumberOfMinutesBetweenDates(commit2.getDate(), mergeCommit.getDate()) * 1f + 1440f
                        + getNumberOfMinutesBetweenDates(mergeCommit.getDate(), mergeCommit2.getDate()) * 1f);
    }

    @Test
    void should_compute_average_cycle_time_given_two_commits_on_a_merged_pr_deploy_on_tag() {
        // Given
        final CommitView commit1 = CommitView.builder().sha(faker.pokemon().name())
                .date(stringToDateTime("2022-01-01 12:00:00"))
                .build();
        final CommitView commit2 = CommitView.builder().sha(faker.pokemon().location())
                .date(stringToDateTime("2022-01-02 12:00:00"))
                .build();
        final CommitView mergeCommit = CommitView.builder().sha(faker.rickAndMorty().location())
                .date(stringToDateTime("2022-01-02 14:00:00"))
                .parentShaList(List.of(commit2.getSha()))
                .build();
        final CommitView mergeCommit2 = CommitView.builder().sha(faker.rickAndMorty().character())
                .date(stringToDateTime("2022-01-03 16:00:00"))
                .parentShaList(List.of(mergeCommit.getSha()))
                .build();


        // When
        final AverageCycleTime averageCycleTime =
                AverageCycleTime.computeCycleTimeForTagRegexToDeploySettings(
                        List.of(
                                PullRequestView.builder()
                                        .commitShaList(
                                                List.of(commit1.getSha(), commit2.getSha())
                                        )
                                        .mergeCommitSha(mergeCommit.getSha())
                                        .mergeDate(mergeCommit.getDate())
                                        .build()
                        ),
                        List.of(TagView.builder().commitSha(mergeCommit2.getSha()).name(faker.name().lastName()).build()),
                        List.of(commit1, commit2, mergeCommit, mergeCommit2)
                );

        // Then
        assertThat(averageCycleTime.getAverageCodingTime()).isEqualTo(1440f);
        assertThat(averageCycleTime.getAverageReviewTime())
                .isEqualTo(getNumberOfMinutesBetweenDates(commit2.getDate(), mergeCommit.getDate()) * 1f);
        assertThat(averageCycleTime.getAverageDeployTime())
                .isEqualTo(getNumberOfMinutesBetweenDates(mergeCommit.getDate(), mergeCommit2.getDate()) * 1f);
        assertThat(averageCycleTime.getAverageValue())
                .isEqualTo(getNumberOfMinutesBetweenDates(commit2.getDate(), mergeCommit.getDate()) * 1f + 1440f
                        + getNumberOfMinutesBetweenDates(mergeCommit.getDate(), mergeCommit2.getDate()) * 1f);
    }


}
