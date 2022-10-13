package io.symeo.monolithic.backend.domain.model.insight;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class DeploymentTest {

    private static final Faker faker = new Faker();

    @Test
    void should_compute_deployment_for_pull_request_merged_on_branch_regex_settings() throws SymeoException {
        // Given
        final String fakePullRequestView1 = faker.cat().name() + "-1";
        final String fakePullRequestView2 = faker.cat().name() + "-2";
        final String fakePullRequestView3 = faker.cat().name() + "-3";
        final Long numberOfDaysBetweenStartDateAndEndDate = 20L;

        final List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate =
                List.of(
                        PullRequestView.builder().id(fakePullRequestView1).base("staging").mergeDate(stringToDate("2022-01-15")).build(),
                        PullRequestView.builder().id(fakePullRequestView2).base("staging").mergeDate(stringToDate("2022-01-17")).build(),
                        PullRequestView.builder().id(fakePullRequestView3).base("staging").mergeDate(stringToDate("2022-01-19")).build()
                );

        final List<PullRequestView> emptyPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate = List.of();

        // When
        final Optional<Deployment> deployment =
                Deployment.computeDeploymentForPullRequestMergedOnBranchRegexSettings(
                        pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                        numberOfDaysBetweenStartDateAndEndDate);

        final Optional<Deployment> emptyDeployment =
                Deployment.computeDeploymentForPullRequestMergedOnBranchRegexSettings(
                        emptyPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                        numberOfDaysBetweenStartDateAndEndDate
                );

        // Then
        assertThat(deployment).isEqualTo(
                Optional.of(Deployment.builder()
                        .deployCount(3)
                        .build())
        );
        assertThat(emptyDeployment).isEqualTo(
                Optional.of(Deployment.builder()
                        .deployCount(0)
                        .build())
        );
    }

    @Test
    void should_compute_deployment_for_tag_regex_to_deploy_settings() throws SymeoException {

        // Given
        final String fakeCommitSha1 = faker.pokemon().name() + "-1";
        final String fakeCommitSha2 = faker.pokemon().name() + "-2";
        final Long numberOfDaysBetweenStartDateAndEndDate = 20L;

        final List<Commit> commitsMatchingTagRegexBetweenStartDateAndEndDate =
                List.of(
                        Commit.builder().sha(fakeCommitSha1).date(stringToDate("2022-01-15")).build(),
                        Commit.builder().sha(fakeCommitSha2).date(stringToDate("2022-01-25")).build()
                );

        final List<Commit> emptyCommitsMatchingTagRegexBetweenStartDateAndEndDate = List.of();

        // When
        final Optional<Deployment> deployment =
                Deployment.computeDeploymentForTagRegexToDeploySettings(
                        commitsMatchingTagRegexBetweenStartDateAndEndDate,
                        numberOfDaysBetweenStartDateAndEndDate);

        final Optional<Deployment> emptyDeployment =
                Deployment.computeDeploymentForTagRegexToDeploySettings(
                        emptyCommitsMatchingTagRegexBetweenStartDateAndEndDate,
                        numberOfDaysBetweenStartDateAndEndDate
                );

        // Then
        assertThat(deployment).isEqualTo(
                Optional.of(Deployment.builder()
                        .deployCount(2)
                        .build())
        );
        assertThat(emptyDeployment).isEqualTo(
                Optional.of(Deployment.builder()
                        .deployCount(0)
                        .build())
        );
    }
}
