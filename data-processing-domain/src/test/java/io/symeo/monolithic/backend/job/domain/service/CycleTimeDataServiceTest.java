package io.symeo.monolithic.backend.job.domain.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.vcs.*;
import io.symeo.monolithic.backend.job.domain.port.out.DataProcessingExpositionStorageAdapter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.time.DateUtils.round;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

public class CycleTimeDataServiceTest {

    private static final Faker faker = new Faker();

    @Test
    void should_not_compute_cycle_times_for_wrong_deploy_detection_type() throws SymeoException {
        // Given
        final DataProcessingExpositionStorageAdapter dataProcessingExpositionStorageAdapter = mock(DataProcessingExpositionStorageAdapter.class);
        final CycleTimeDataFactory cycleTimeDataFactory = mock(CycleTimeDataFactory.class);
        final CycleTimeDataService cycleTimeDataService = new CycleTimeDataService(dataProcessingExpositionStorageAdapter, cycleTimeDataFactory);

        final UUID organizationId = UUID.randomUUID();
        final Repository repository = Repository.builder()
                .id(faker.cat().name())
                .name(faker.name().name())
                .organizationId(organizationId)
                .vcsOrganizationId(organizationId.toString())
                .vcsOrganizationName(faker.name().name())
                .build();

        final String deployDetectionType = faker.rickAndMorty().character();
        final String pullRequestMergedOnBranchRegex = "main";
        final String tagRegex = faker.gameOfThrones().dragon();
        final List<String> excludeBranchRegex = List.of("^staging$");

        final PullRequest pullRequest1 = PullRequest.builder()
                .id(faker.dog().name() + "-PR-1")
                .number(faker.number().numberBetween(1,100))
                .head(faker.pokemon().name())
                .build();
        final PullRequest pullRequest2 = PullRequest.builder()
                .id(faker.dog().name() + "-PR-2")
                .number(faker.number().numberBetween(1,100))
                .head(faker.pokemon().name())
                .build();
        final PullRequest pullRequestToFilter = PullRequest.builder()
                .id(faker.dog().name() + "-PR-3")
                .number(faker.number().numberBetween(1,100))
                .head("staging")
                .build();
        final List<PullRequest> pullRequests = List.of(pullRequest1, pullRequest2, pullRequestToFilter);

        // When
        final List<CycleTime> cycleTimes = cycleTimeDataService.computeCycleTimesForRepository(
                repository, pullRequests, deployDetectionType, pullRequestMergedOnBranchRegex, tagRegex, excludeBranchRegex);

        // Then
        assertThat(cycleTimes).isEqualTo(List.of());
    }

    @Test
    void should_compute_cycle_times_for_pull_requests_merged_on_branch_regex() throws SymeoException {
        // Given
        final DataProcessingExpositionStorageAdapter dataProcessingExpositionStorageAdapter = mock(DataProcessingExpositionStorageAdapter.class);
        final CycleTimeDataFactory cycleTimeDataFactory = mock(CycleTimeDataFactory.class);
        final CycleTimeDataService cycleTimeDataService = new CycleTimeDataService(dataProcessingExpositionStorageAdapter, cycleTimeDataFactory);

        final UUID organizationId = UUID.randomUUID();
        final Repository repository = Repository.builder()
                .id(faker.cat().name())
                .name(faker.name().name())
                .organizationId(organizationId)
                .vcsOrganizationId(organizationId.toString())
                .vcsOrganizationName(faker.name().name())
                .build();

        final String deployDetectionType = "pull_request";
        final String pullRequestMergedOnBranchRegex = "main";
        final String tagRegex = faker.gameOfThrones().dragon();
        final List<String> excludeBranchRegex = List.of("^staging$");

        final Commit commit1 = Commit.builder().sha(faker.dragonBall().character() + "-commit-1").build();
        final Commit commit2 = Commit.builder().sha(faker.dragonBall().character() + "-commit-2").build();
        final Commit commit3 = Commit.builder().sha(faker.dragonBall().character() + "-commit-3").build();
        final List<Commit> commitsForRepository = List.of(commit1, commit2, commit3);

        final PullRequest pullRequest1 = PullRequest.builder()
                .id(faker.dog().name() + "-PR-1")
                .number(faker.number().numberBetween(1,100))
                .head(faker.pokemon().name())
                .build();
        final PullRequest pullRequest2 = PullRequest.builder()
                .id(faker.dog().name() + "-PR-2")
                .number(faker.number().numberBetween(1,100))
                .head(faker.pokemon().name())
                .build();
        final PullRequest pullRequestToFilter = PullRequest.builder()
                .id(faker.dog().name() + "-PR-3")
                .number(faker.number().numberBetween(1,100))
                .head("staging")
                .build();
        final List<PullRequest> pullRequests = List.of(pullRequest1, pullRequest2, pullRequestToFilter);

        final PullRequest pullRequestMergedOnBranchRegex1 = PullRequest.builder()
                .id(faker.cat().name() + "-merged-PR-1")
                .number(faker.number().numberBetween(1,100))
                .base("main")
                .build();
        final PullRequest pullRequestMergedOnBranchRegex2 = PullRequest.builder()
                .id(faker.cat().name() + "-merged-PR-2")
                .number(faker.number().numberBetween(1,100))
                .base("main")
                .build();
        final PullRequest pullRequestMergedOnBranchRegex3 = PullRequest.builder()
                .id(faker.cat().name() + "-merged-PR-2")
                .number(faker.number().numberBetween(1,100))
                .base("test")
                .build();
        final List<PullRequest> pullRequestsMergedOnMatchedBranches = List.of(
                pullRequestMergedOnBranchRegex1,
                pullRequestMergedOnBranchRegex2,
                pullRequestMergedOnBranchRegex3
        );

        final CycleTime cycleTime1 = CycleTime.builder()
                .id(faker.dragonBall().character())
                .value(300L)
                .codingTime(100L)
                .reviewTime(100L)
                .timeToDeploy(100L)
                .build();
        final CycleTime cycleTime2 = CycleTime.builder()
                .id(faker.dragonBall().character())
                .value(600L)
                .codingTime(200L)
                .reviewTime(200L)
                .timeToDeploy(200L)
                .build();

        // When
        when(dataProcessingExpositionStorageAdapter.readAllCommitsForRepositoryId(repository.getId()))
                .thenReturn(commitsForRepository);
        when(dataProcessingExpositionStorageAdapter.readMergedPullRequestsForRepositoryIdUntilEndDate(repository.getId(), round(new Date(), Calendar.MINUTE)))
                .thenReturn(pullRequestsMergedOnMatchedBranches);
        when(cycleTimeDataFactory.computeCycleTimeForMergeOnPullRequestMatchingDeliverySettings(
                pullRequest1, List.of(pullRequestMergedOnBranchRegex1, pullRequestMergedOnBranchRegex2), commitsForRepository))
                .thenReturn(cycleTime1);
        when(cycleTimeDataFactory.computeCycleTimeForMergeOnPullRequestMatchingDeliverySettings(
                pullRequest2, List.of(pullRequestMergedOnBranchRegex1, pullRequestMergedOnBranchRegex2), commitsForRepository))
                .thenReturn(cycleTime2);

        final List<CycleTime> cycleTimes = cycleTimeDataService.computeCycleTimesForRepository(
                repository,
                pullRequests,
                deployDetectionType,
                pullRequestMergedOnBranchRegex,
                tagRegex,
                excludeBranchRegex
        );

        // Then
        assertThat(cycleTimes.size()).isEqualTo(2);
        assertThat(cycleTimes.get(0)).isEqualTo(cycleTime1);
        assertThat(cycleTimes.get(1)).isEqualTo(cycleTime2);
    }

    @Test
    void should_compute_cycle_times_for_tag_regex_to_deploy_settings() throws SymeoException {
        // Given
        final DataProcessingExpositionStorageAdapter dataProcessingExpositionStorageAdapter = mock(DataProcessingExpositionStorageAdapter.class);
        final CycleTimeDataFactory cycleTimeDataFactory = mock(CycleTimeDataFactory.class);
        final CycleTimeDataService cycleTimeDataService = new CycleTimeDataService(dataProcessingExpositionStorageAdapter, cycleTimeDataFactory);

        final UUID organizationId = UUID.randomUUID();
        final Repository repository = Repository.builder()
                .id(faker.cat().name())
                .name(faker.name().name())
                .organizationId(organizationId)
                .vcsOrganizationId(organizationId.toString())
                .vcsOrganizationName(faker.name().name())
                .build();

        final String deployDetectionType = "tag";
        final String pullRequestMergedOnBranchRegex = faker.gameOfThrones().character();
        final String tagRegex = "^deploy$";
        final List<String> excludeBranchRegex = List.of("^staging$");

        final Commit commit1 = Commit.builder().sha(faker.dragonBall().character() + "-commit-1").build();
        final Commit commit2 = Commit.builder().sha(faker.dragonBall().character() + "-commit-2").build();
        final Commit commit3 = Commit.builder().sha(faker.dragonBall().character() + "-commit-3").build();
        final List<Commit> commitsForRepository = List.of(commit1, commit2, commit3);

        final PullRequest pullRequest1 = PullRequest.builder()
                .id(faker.dog().name() + "-PR-1")
                .number(faker.number().numberBetween(1,100))
                .head(faker.pokemon().name())
                .build();
        final PullRequest pullRequest2 = PullRequest.builder()
                .id(faker.dog().name() + "-PR-2")
                .number(faker.number().numberBetween(1,100))
                .head(faker.pokemon().name())
                .build();
        final PullRequest pullRequestToFilter = PullRequest.builder()
                .id(faker.dog().name() + "-PR-3")
                .number(faker.number().numberBetween(1,100))
                .head("staging")
                .build();
        final List<PullRequest> pullRequests = List.of(pullRequest1, pullRequest2, pullRequestToFilter);

        final Tag tag1 = Tag.builder()
                .name("deploy")
                .commitSha(commit1.getSha())
                .repositoryId(repository.getId())
                .vcsUrl(faker.backToTheFuture().character())
                .build();
        final Tag tag2 = Tag.builder()
                .name("deploy")
                .commitSha(commit2.getSha())
                .repositoryId(repository.getId())
                .vcsUrl(faker.backToTheFuture().character())
                .build();
        final Tag tag3 = Tag.builder()
                .name("wrong-tag")
                .commitSha(commit3.getSha())
                .repositoryId(repository.getId())
                .vcsUrl(faker.backToTheFuture().character())
                .build();
        final List<Tag> tags = List.of(tag1, tag2, tag3);

        final CycleTime cycleTime1 = CycleTime.builder()
                .id(faker.dragonBall().character())
                .value(300L)
                .codingTime(100L)
                .reviewTime(100L)
                .timeToDeploy(100L)
                .build();
        final CycleTime cycleTime2 = CycleTime.builder()
                .id(faker.dragonBall().character())
                .value(600L)
                .codingTime(200L)
                .reviewTime(200L)
                .timeToDeploy(200L)
                .build();

        // When
        when(dataProcessingExpositionStorageAdapter.readAllCommitsForRepositoryId(repository.getId()))
                .thenReturn(commitsForRepository);
        when(dataProcessingExpositionStorageAdapter.readTagsForRepositoryId(repository.getId()))
                .thenReturn(tags);
        when(cycleTimeDataFactory.computeCycleTimeForTagRegexToDeploySettings(
                pullRequest1,
                List.of(tag1, tag2),
                commitsForRepository))
                .thenReturn(cycleTime1);
        when(cycleTimeDataFactory.computeCycleTimeForTagRegexToDeploySettings(
                pullRequest2,
                List.of(tag1, tag2),
                commitsForRepository))
                .thenReturn(cycleTime2);

        final List<CycleTime> cycleTimes = cycleTimeDataService.computeCycleTimesForRepository(
                repository,
                pullRequests,
                deployDetectionType,
                pullRequestMergedOnBranchRegex,
                tagRegex,
                excludeBranchRegex
        );

        // Then
        assertThat(cycleTimes.size()).isEqualTo(2);
        assertThat(cycleTimes.get(0)).isEqualTo(cycleTime1);
        assertThat(cycleTimes.get(1)).isEqualTo(cycleTime2);
    }
}
