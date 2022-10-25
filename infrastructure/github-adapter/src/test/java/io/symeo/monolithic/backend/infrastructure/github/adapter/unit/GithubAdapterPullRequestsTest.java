package io.symeo.monolithic.backend.infrastructure.github.adapter.unit;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.github.adapter.GithubAdapter;
import io.symeo.monolithic.backend.infrastructure.github.adapter.GithubHttpApiClient;
import io.symeo.monolithic.backend.job.domain.github.dto.pr.GithubCommentsDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.pr.GithubCommitsDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.pr.GithubPullRequestDTO;
import io.symeo.monolithic.backend.job.domain.github.mapper.GithubMapper;
import io.symeo.monolithic.backend.job.domain.github.properties.GithubProperties;
import io.symeo.monolithic.backend.job.domain.model.vcs.PullRequest;
import io.symeo.monolithic.backend.job.domain.model.vcs.Repository;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class GithubAdapterPullRequestsTest extends AbstractGithubAdapterTest {


    @Test
    void should_get_pull_requests_given_a_repository() throws IOException, SymeoException {
        // Given
        final String token = faker.pokemon().name();
        final GithubProperties githubProperties = new GithubProperties();
        githubProperties.setSize(3);
        final GithubHttpApiClient githubHttpApiClient = mock(GithubHttpApiClient.class);
        final GithubAdapter githubAdapter = new GithubAdapter(githubHttpApiClient,
                githubProperties, objectMapper);
        final Repository repository =
                Repository.builder().vcsOrganizationName(faker.name().lastName()).name(faker.name().firstName()).build();
        final Map<String, String> authorization = Map.of("Authorization", "token " + token);
        final GithubPullRequestDTO[] githubPullRequestStubs1 = getStubsFromClassT("get_pull_requests_for_repo",
                "get_pr_for_repo_page_1_size_3.json", GithubPullRequestDTO[].class);
        final GithubPullRequestDTO[] githubPullRequestStubs2 = getStubsFromClassT("get_pull_requests_for_repo",
                "get_pr_for_repo_page_2_size_3.json", GithubPullRequestDTO[].class);
        final GithubPullRequestDTO[] githubPullRequestStubs3 = getStubsFromClassT("get_pull_requests_for_repo",
                "get_pr_for_repo_page_3_size_3.json", GithubPullRequestDTO[].class);
        final GithubPullRequestDTO pr74 = getStubsFromClassT(
                "get_pull_request_details_for_pr_id", "get_pr_74.json",
                GithubPullRequestDTO.class);
        final GithubPullRequestDTO pr75 = getStubsFromClassT(
                "get_pull_request_details_for_pr_id", "get_pr_75.json",
                GithubPullRequestDTO.class);
        final GithubPullRequestDTO pr76 = getStubsFromClassT(
                "get_pull_request_details_for_pr_id", "get_pr_76.json",
                GithubPullRequestDTO.class);
        final GithubPullRequestDTO pr77 = getStubsFromClassT(
                "get_pull_request_details_for_pr_id", "get_pr_77.json",
                GithubPullRequestDTO.class);
        final GithubPullRequestDTO pr78 = getStubsFromClassT(
                "get_pull_request_details_for_pr_id", "get_pr_78.json",
                GithubPullRequestDTO.class);
        final GithubPullRequestDTO pr79 = getStubsFromClassT(
                "get_pull_request_details_for_pr_id", "get_pr_79.json",
                GithubPullRequestDTO.class);
        final GithubPullRequestDTO pr80 = getStubsFromClassT(
                "get_pull_request_details_for_pr_id", "get_pr_80.json",
                GithubPullRequestDTO.class);

        // When
        // All PRs by repo
        when(githubHttpApiClient.getPullRequestsForRepositoryAndOrganizationOrderByDescDate(repository.getVcsOrganizationName(),
                repository.getName(), 1, 3)).thenReturn(githubPullRequestStubs1);
        when(githubHttpApiClient.getPullRequestsForRepositoryAndOrganizationOrderByDescDate(repository.getVcsOrganizationName(),
                repository.getName(), 2, 3)).thenReturn(githubPullRequestStubs2);
        when(githubHttpApiClient.getPullRequestsForRepositoryAndOrganizationOrderByDescDate(repository.getVcsOrganizationName(),
                repository.getName(), 3, 3)).thenReturn(githubPullRequestStubs3);

        // PR details by PR
        when(githubHttpApiClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), 74)).thenReturn(pr74);
        when(githubHttpApiClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), 75)).thenReturn(pr75);
        when(githubHttpApiClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), 76)).thenReturn(pr76);
        when(githubHttpApiClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), 77)).thenReturn(pr77);
        when(githubHttpApiClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), 78)).thenReturn(pr78);
        when(githubHttpApiClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), 79)).thenReturn(pr79);
        when(githubHttpApiClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), 80)).thenReturn(pr80);

        final byte[] rawPullRequestsForRepository = githubAdapter.getRawPullRequestsForRepository(repository, null);

        // Then
        verify(githubHttpApiClient, times(3)).getPullRequestsForRepositoryAndOrganizationOrderByDescDate(any(), any(),
                any(), any());
        verify(githubHttpApiClient, times(7)).getPullRequestDetailsForPullRequestNumber(any(), any(), any());
        final List<PullRequest> expectedResults = Stream.of(pr74, pr75,
                pr76, pr77,
                pr78, pr79, pr80).map(pr -> GithubMapper.mapPullRequestDtoToDomain(pr, githubAdapter.getName())).toList();
        final List<PullRequest> pullRequestList = githubAdapter.pullRequestsBytesToDomain(rawPullRequestsForRepository);
        assertThat(pullRequestList).containsAll(expectedResults);
    }

    @Test
    void should_get_incremental_pull_requests_given_a_repository_and_already_collected_pull_requests() throws IOException, SymeoException {
        // Given
        final String token = faker.pokemon().name();
        final GithubProperties githubProperties = new GithubProperties();
        githubProperties.setSize(10);
        final GithubHttpApiClient githubHttpApiClient = mock(GithubHttpApiClient.class);
        final GithubAdapter githubAdapter = new GithubAdapter(githubHttpApiClient,
                githubProperties, objectMapper);
        final Repository repository =
                Repository.builder().vcsOrganizationName(faker.name().lastName()).name(faker.name().firstName()).build();
        final Map<String, String> authorization = Map.of("Authorization", "token " + token);
        final GithubPullRequestDTO[] githubPullRequestStubs1 = getStubsFromClassT("get_pull_requests_for_repo",
                "incremental_get_pr_for_repo_page_1_size_10.json", GithubPullRequestDTO[].class);
        final GithubPullRequestDTO pr74 = getStubsFromClassT(
                "get_pull_request_details_for_pr_id", "get_pr_74.json",
                GithubPullRequestDTO.class);
        final GithubPullRequestDTO pr75 = getStubsFromClassT(
                "get_pull_request_details_for_pr_id", "get_pr_75.json",
                GithubPullRequestDTO.class);
        final GithubPullRequestDTO pr76 = getStubsFromClassT(
                "get_pull_request_details_for_pr_id", "get_pr_76.json",
                GithubPullRequestDTO.class);
        final GithubPullRequestDTO pr77 = getStubsFromClassT(
                "get_pull_request_details_for_pr_id", "get_pr_77.json",
                GithubPullRequestDTO.class);
        final GithubPullRequestDTO pr78 = getStubsFromClassT(
                "get_pull_request_details_for_pr_id", "get_pr_78.json",
                GithubPullRequestDTO.class);
        final GithubPullRequestDTO pr79 = getStubsFromClassT(
                "get_pull_request_details_for_pr_id", "get_pr_79.json",
                GithubPullRequestDTO.class);
        final byte[] rawPullRequestsAlreadyCollected = objectMapper.writeValueAsBytes(new GithubPullRequestDTO[]{pr76
                , pr77, pr78});

        // When
        // All PRs by repo
        when(githubHttpApiClient.getPullRequestsForRepositoryAndOrganizationOrderByDescDate(repository.getVcsOrganizationName(),
                repository.getName(), 1, 10))
                .thenReturn(githubPullRequestStubs1);
        // PR details by PR
        when(githubHttpApiClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), 74))
                .thenReturn(pr74);
        when(githubHttpApiClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), 75))
                .thenReturn(pr75);
        when(githubHttpApiClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), 76))
                .thenReturn(pr76);
        when(githubHttpApiClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), 79))
                .thenReturn(pr79);
        final byte[] rawPullRequestsForRepository = githubAdapter.getRawPullRequestsForRepository(repository,
                rawPullRequestsAlreadyCollected);

        // Then
        verify(githubHttpApiClient, times(1)).getPullRequestsForRepositoryAndOrganizationOrderByDescDate(any(), any(),
                any(), any());
        verify(githubHttpApiClient, times(4)).getPullRequestDetailsForPullRequestNumber(anyString(), any(), any());
        final List<PullRequest> expectedResults =
                Stream.of(pr74, pr75, pr76, pr77, pr78, pr79).map(pr -> GithubMapper.mapPullRequestDtoToDomain(pr,
                        githubAdapter.getName())).toList();
        final List<PullRequest> pullRequestList = githubAdapter.pullRequestsBytesToDomain(rawPullRequestsForRepository);
        assertThat(pullRequestList).containsAll(expectedResults);
    }


    @Test
    void should_return_empty_pull_requests_list_given_an_empty_byte_array_to_map_to_domain() throws SymeoException {
        // Given
        final byte[] emptyBytes = new byte[0];
        final GithubProperties githubProperties = new GithubProperties();
        githubProperties.setSize(3);
        final GithubAdapter githubAdapter = new GithubAdapter(mock(GithubHttpApiClient.class),
                githubProperties, objectMapper);

        // When
        final List<PullRequest> pullRequestList = githubAdapter.pullRequestsBytesToDomain(emptyBytes);

        // Then
        assertThat(pullRequestList).isEmpty();
    }


    @Test
    void should_map_github_pr_dto_to_pull_request_domain() throws IOException {
        // Given
        final GithubPullRequestDTO pr80 = getStubsFromClassT(
                "get_pull_request_details_for_pr_id", "get_pr_80.json",
                GithubPullRequestDTO.class);
        final String githubPlatformName = "github";

        // When
        final PullRequest pullRequest = GithubMapper.mapPullRequestDtoToDomain(pr80, githubPlatformName);

        // Then
        assertThat(pullRequest.getId()).isEqualTo(githubPlatformName + "-" + pr80.getId());
        assertThat(pullRequest.getCommitNumber()).isEqualTo(pr80.getCommits());
        assertThat(pullRequest.getDeletedLineNumber()).isEqualTo(pr80.getDeletions());
        assertThat(pullRequest.getAddedLineNumber()).isEqualTo(pr80.getAdditions());
        assertThat(pullRequest.getCreationDate()).isEqualTo(pr80.getCreatedAt());
        assertThat(pullRequest.getLastUpdateDate()).isEqualTo(pr80.getUpdatedAt());
        assertThat(pullRequest.getMergeDate()).isEqualTo(pr80.getMergedAt());
        assertThat(pullRequest.getIsMerged()).isEqualTo(pr80.getMerged());
        assertThat(pullRequest.getIsDraft()).isEqualTo(pr80.getDraft());
        assertThat(pullRequest.getStatus()).isEqualTo(pr80.getState());
        assertThat(pullRequest.getNumber()).isEqualTo(pr80.getNumber());
        assertThat(pullRequest.getVcsUrl()).isEqualTo(pr80.getHtmlUrl());
        assertThat(pullRequest.getTitle()).isEqualTo(pr80.getTitle());
        assertThat(pullRequest.getAuthorLogin()).isEqualTo(pr80.getUser().getLogin());
        assertThat(pullRequest.getHead()).isEqualTo(pr80.getHead().getRef());
        assertThat(pullRequest.getBase()).isEqualTo(pr80.getBase().getRef());
    }

    @Test
    void should_get_pull_request_with_size_exactly_the_same_than_pagination_size() throws SymeoException, IOException {
        // Given
        final String token = faker.pokemon().name();
        final GithubProperties properties = new GithubProperties();
        properties.setSize(1);
        final GithubHttpApiClient githubHttpApiClient = mock(GithubHttpApiClient.class);
        final GithubAdapter githubAdapter = new GithubAdapter(githubHttpApiClient,
                properties, objectMapper);
        final Repository repository =
                Repository.builder().vcsOrganizationName(faker.name().lastName()).name(faker.name().firstName()).build();
        final GithubPullRequestDTO[] githubPullRequestStubs1 = getStubsFromClassT("get_pull_requests_for_repo",
                "get_pr_for_repo_page_1_size_1.json", GithubPullRequestDTO[].class);
        final GithubPullRequestDTO githubPullRequestDetails = getStubsFromClassT("get_pull_request_details_for_pr_id",
                "get_pr_76.json", GithubPullRequestDTO.class);
        final GithubCommitsDTO[] githubCommitsStubs1 = getStubsFromClassT("get_commits_for_pr_number",
                "get_commits_for_pr_number_2_page_1_size_30.json",
                GithubCommitsDTO[].class);
        final GithubCommentsDTO[] githubCommentsStubs1 = getStubsFromClassT("get_comments_for_pr_number",
                "get_comments_for_pr_number_2_page_1_size_3.json",
                GithubCommentsDTO[].class);

        // When
        when(githubHttpApiClient.getPullRequestsForRepositoryAndOrganizationOrderByDescDate(repository.getVcsOrganizationName(), repository.getName(), 1, properties.getSize()))
                .thenReturn(githubPullRequestStubs1);
        when(githubHttpApiClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), 80))
                .thenReturn(githubPullRequestDetails);
        when(githubHttpApiClient.getCommitsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), 80, 1, properties.getSize()))
                .thenReturn(githubCommitsStubs1);
        when(githubHttpApiClient.getCommentsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), 80, 1, properties.getSize()))
                .thenReturn(githubCommentsStubs1);

        final byte[] exactSizeRawPullRequestsForRepository = githubAdapter.getRawPullRequestsForRepository(repository
                , null);
        final GithubPullRequestDTO[] exactSizeGithubPullRequestDTOSResult =
                githubAdapter.bytesToDto(exactSizeRawPullRequestsForRepository, GithubPullRequestDTO[].class);

        // Then
        assertThat(exactSizeGithubPullRequestDTOSResult.length).isEqualTo(githubPullRequestStubs1.length);
        assertThat(exactSizeGithubPullRequestDTOSResult[0].getId()).isEqualTo(76);
    }
}
