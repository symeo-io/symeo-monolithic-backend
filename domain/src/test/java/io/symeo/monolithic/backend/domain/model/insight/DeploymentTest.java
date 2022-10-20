package io.symeo.monolithic.backend.domain.model.insight;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class DeploymentTest {

    private static final Faker faker = new Faker();

    @Test
    void should_compute_deployment_for_pull_request_merged_on_branch_regex_settings() throws SymeoException {
        // Given
        final String fakePullRequestView1 = faker.cat().name() + "-1";
        final String fakePullRequestView2 = faker.cat().name() + "-2";
        final String fakePullRequestView3 = faker.cat().name() + "-3";

        final String fakeRepositoryName1 = faker.dog().name() + "-1";
        final String fakeRepositoryName2 = faker.dog().name() + "-2";
        final String fakeRepositoryName3 = faker.dog().name() + "-3";

        final Long numberOfDaysBetweenStartDateAndEndDate = 20L;
        final LocalDateTime now = LocalDateTime.now();

        final List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate =
                List.of(
                        PullRequestView.builder().id(fakePullRequestView1).base("staging").mergeDate(stringToDate("2022-01-15")).repository(fakeRepositoryName1).build(),
                        PullRequestView.builder().id(fakePullRequestView2).base("staging").mergeDate(stringToDate("2022-01-17")).repository(fakeRepositoryName2).build(),
                        PullRequestView.builder().id(fakePullRequestView3).base("staging").mergeDate(stringToDate("2022-01-20")).repository(fakeRepositoryName3).build()
                );

        final List<PullRequestView> soloPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate =
                List.of(
                        PullRequestView.builder().id(fakePullRequestView1).base("staging").mergeDate(stringToDate("2022-01-15")).repository(fakeRepositoryName1).build()
                );

        final List<PullRequestView> emptyPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate = List.of();

        // When
        final Optional<Deployment> deployment =
                Deployment.computeDeploymentForPullRequestMergedOnBranchRegexSettings(
                        pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                        numberOfDaysBetweenStartDateAndEndDate);
        final Optional<Deployment> soloDeployment =
                Deployment.computeDeploymentForPullRequestMergedOnBranchRegexSettings(
                        soloPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                        numberOfDaysBetweenStartDateAndEndDate);
        final Optional<Deployment> emptyDeployment =
                Deployment.computeDeploymentForPullRequestMergedOnBranchRegexSettings(
                        emptyPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                        numberOfDaysBetweenStartDateAndEndDate);

        // Then
        assertThat(deployment).isEqualTo(
                Optional.of(Deployment.builder()
                        .deployCount(3)
                        .deploysPerDay(0.2f)
                        .averageTimeBetweenDeploys(3600.0f)
                        .lastDeployDuration((float) MINUTES.between(stringToDate("2022-01-20").toInstant(), now.atZone(ZoneId.of("Europe/Paris")).toInstant()))
                        .lastDeployRepository(fakeRepositoryName3)
                        .build())
        );
        assertThat(soloDeployment).isEqualTo(
                Optional.of(Deployment.builder()
                        .deployCount(1)
                        .deploysPerDay(0.1f)
                        .averageTimeBetweenDeploys(null)
                        .lastDeployDuration((float) MINUTES.between(stringToDate("2022-01-15").toInstant(), now.atZone(ZoneId.of("Europe/Paris")).toInstant()))
                        .lastDeployRepository(fakeRepositoryName1)
                        .build())
        );
        assertThat(emptyDeployment).isEqualTo(
                Optional.of(Deployment.builder()
                        .deployCount(0)
                        .deploysPerDay(null)
                        .averageTimeBetweenDeploys(null)
                        .lastDeployDuration(null)
                        .lastDeployRepository(null)
                        .build())
        );
    }

    @Test
    void should_compute_deployment_for_tag_regex_to_deploy_settings() throws SymeoException {

        // Given
        final String fakeCommitSha1 = faker.pokemon().name() + "-1";
        final String fakeCommitSha2 = faker.pokemon().name() + "-2";
        final String fakeCommitSha3 = faker.pokemon().name() + "-3";
        final String fakeCommitSha4 = faker.pokemon().name() + "-4";

        final String fakeRepositoryId1 = faker.gameOfThrones().character() + "-1";
        final String fakeRepositoryId2 = faker.gameOfThrones().character() + "-2";
        final String fakeRepositoryId3 = faker.gameOfThrones().character() + "-3";

        final String fakeRepositoryName1 = faker.dog().name() + "-1";
        final String fakeRepositoryName2 = faker.dog().name() + "-2";
        final String fakeRepositoryName3 = faker.dog().name() + "-3";

        final String fakeTagName = faker.cat().name() + "-tag-1";

        final String fakeVcsUrl = faker.gameOfThrones().character();

        final Long numberOfDaysBetweenStartDateAndEndDate = 20L;
        final LocalDateTime now = LocalDateTime.now();

        final List<Commit> commitsMatchingTagRegexBetweenStartDateAndEndDate =
                List.of(
                        Commit.builder().sha(fakeCommitSha1).date(stringToDate("2022-01-15")).repositoryId(fakeRepositoryId1).build(),
                        Commit.builder().sha(fakeCommitSha2).date(stringToDate("2022-01-25")).repositoryId(fakeRepositoryId2).build(),
                        Commit.builder().sha(fakeCommitSha3).date(stringToDate("2022-01-26")).repositoryId(fakeRepositoryId2).build(),
                        Commit.builder().sha(fakeCommitSha4).date(stringToDate("2022-01-30")).repositoryId(fakeRepositoryId3).build()
                );
        final List<Commit> soloCommitsMatchingTagRegexBetweenStartDateAndEndDate =
                List.of(
                        Commit.builder().sha(fakeCommitSha1).date(stringToDate("2022-01-15")).repositoryId(fakeRepositoryId1).build()
                );

        final List<Commit> emptyCommitsMatchingTagRegexBetweenStartDateAndEndDate = List.of();

        final List<Tag> tagsMatchingTeamIdAndDeployTagRegex = List.of(
                Tag.builder().commitSha(fakeCommitSha1).repository(Repository.builder().id(fakeRepositoryId1).name(fakeRepositoryName1).build()).name(fakeTagName).vcsUrl(fakeVcsUrl).build(),
                Tag.builder().commitSha(fakeCommitSha2).repository(Repository.builder().id(fakeRepositoryId2).name(fakeRepositoryName2).build()).name(fakeTagName).vcsUrl(fakeVcsUrl).build(),
                Tag.builder().commitSha(fakeCommitSha3).repository(Repository.builder().id(fakeRepositoryId3).name(fakeRepositoryName3).build()).name(fakeTagName).vcsUrl(fakeVcsUrl).build(),
                Tag.builder().commitSha(fakeCommitSha4).repository(Repository.builder().id(fakeRepositoryId3).name(fakeRepositoryName3).build()).name(fakeTagName).vcsUrl(fakeVcsUrl).build()
        );

        // When
        final Optional<Deployment> deployment =
                Deployment.computeDeploymentForTagRegexToDeploySettings(commitsMatchingTagRegexBetweenStartDateAndEndDate,
                        numberOfDaysBetweenStartDateAndEndDate,
                        tagsMatchingTeamIdAndDeployTagRegex);
        final Optional<Deployment> soloDeployment =
                Deployment.computeDeploymentForTagRegexToDeploySettings(soloCommitsMatchingTagRegexBetweenStartDateAndEndDate,
                        numberOfDaysBetweenStartDateAndEndDate,
                        tagsMatchingTeamIdAndDeployTagRegex);
        final Optional<Deployment> emptyDeployment =
                Deployment.computeDeploymentForTagRegexToDeploySettings(emptyCommitsMatchingTagRegexBetweenStartDateAndEndDate,
                        numberOfDaysBetweenStartDateAndEndDate,
                        tagsMatchingTeamIdAndDeployTagRegex);

        // Then
        assertThat(deployment).isEqualTo(
                Optional.of(Deployment.builder()
                        .deployCount(4)
                        .deploysPerDay(0.2f)
                        .averageTimeBetweenDeploys(7200.0f)
                        .lastDeployDuration((float) MINUTES.between(stringToDate("2022-01-30").toInstant(), now.atZone(ZoneId.of("Europe/Paris")).toInstant()))
                        .lastDeployRepository(fakeRepositoryName3)
                        .lastDeployLink(fakeVcsUrl)
                        .build())
        );
        assertThat(soloDeployment).isEqualTo(
                Optional.of(Deployment.builder()
                        .deployCount(1)
                        .deploysPerDay(0.1f)
                        .averageTimeBetweenDeploys(null)
                        .lastDeployDuration((float) MINUTES.between(stringToDate("2022-01-15").toInstant(), now.atZone(ZoneId.of("Europe/Paris")).toInstant()))
                        .lastDeployRepository(fakeRepositoryName1)
                        .lastDeployLink(fakeVcsUrl)
                        .build())
        );
        assertThat(emptyDeployment).isEqualTo(
                Optional.of(Deployment.builder()
                        .deployCount(0)
                        .deploysPerDay(null)
                        .averageTimeBetweenDeploys(null)
                        .lastDeployDuration(null)
                        .lastDeployRepository(null)
                        .lastDeployLink(null)
                        .build())
        );
    }
}
