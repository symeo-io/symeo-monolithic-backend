package fr.catlean.monolithic.backend.infrastructure.github.adapter.unit;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.GithubAdapter;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.client.GithubHttpClient;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.dto.pr.GithubPullRequestDTO;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.properties.GithubProperties;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static fr.catlean.monolithic.backend.infrastructure.github.adapter.mapper.GithubMapper.mapPullRequestDtoToDomain;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class GithubAdapterPullRequestsTest extends AbstractGithubAdapterTest {


    @Test
    void should_get_pull_requests_given_a_repository() throws IOException, CatleanException {
        // Given
        final String token = faker.pokemon().name();
        final GithubProperties githubProperties = new GithubProperties();
        githubProperties.setSize(3);
        final GithubHttpClient githubHttpClient = mock(GithubHttpClient.class);
        final GithubAdapter githubAdapter = new GithubAdapter(githubHttpClient,
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
        when(githubHttpClient.getPullRequestsForRepositoryAndOrganization(repository.getVcsOrganizationName(),
                repository.getName(), 1, 3)).thenReturn(githubPullRequestStubs1);
        when(githubHttpClient.getPullRequestsForRepositoryAndOrganization(repository.getVcsOrganizationName(),
                repository.getName(), 2, 3)).thenReturn(githubPullRequestStubs2);
        when(githubHttpClient.getPullRequestsForRepositoryAndOrganization(repository.getVcsOrganizationName(),
                repository.getName(), 3, 3)).thenReturn(githubPullRequestStubs3);

        // PR details by PR
        when(githubHttpClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), 74)).thenReturn(pr74);
        when(githubHttpClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), 75)).thenReturn(pr75);
        when(githubHttpClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), 76)).thenReturn(pr76);
        when(githubHttpClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), 77)).thenReturn(pr77);
        when(githubHttpClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), 78)).thenReturn(pr78);
        when(githubHttpClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), 79)).thenReturn(pr79);
        when(githubHttpClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), 80)).thenReturn(pr80);

        final byte[] rawPullRequestsForRepository = githubAdapter.getRawPullRequestsForRepository(repository, null);

        // Then
        verify(githubHttpClient, times(3)).getPullRequestsForRepositoryAndOrganization(any(), any(), any(), any());
        verify(githubHttpClient, times(7)).getPullRequestDetailsForPullRequestNumber(any(), any(), any());
        final List<PullRequest> expectedResults = Stream.of(pr74, pr75,
                pr76, pr77,
                pr78, pr79, pr80).map(pr -> mapPullRequestDtoToDomain(pr, githubAdapter.getName())).toList();
        final List<PullRequest> pullRequestList = githubAdapter.pullRequestsBytesToDomain(rawPullRequestsForRepository);
        assertThat(pullRequestList).containsAll(expectedResults);
    }

    @Test
    void should_get_incremental_pull_requests_given_a_repository_and_already_collected_pull_requests() throws IOException, CatleanException {
        // Given
        final String token = faker.pokemon().name();
        final GithubProperties githubProperties = new GithubProperties();
        githubProperties.setSize(10);
        final GithubHttpClient githubHttpClient = mock(GithubHttpClient.class);
        final GithubAdapter githubAdapter = new GithubAdapter(githubHttpClient,
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
        when(githubHttpClient.getPullRequestsForRepositoryAndOrganization(repository.getVcsOrganizationName(),
                repository.getName(), 1, 10))
                .thenReturn(githubPullRequestStubs1);
        // PR details by PR
        when(githubHttpClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), 74))
                .thenReturn(pr74);
        when(githubHttpClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), 75))
                .thenReturn(pr75);
        when(githubHttpClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), 76))
                .thenReturn(pr76);
        when(githubHttpClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), 79))
                .thenReturn(pr79);
        final byte[] rawPullRequestsForRepository = githubAdapter.getRawPullRequestsForRepository(repository,
                rawPullRequestsAlreadyCollected);

        // Then
        verify(githubHttpClient, times(1)).getPullRequestsForRepositoryAndOrganization(any(), any(), any(), any());
        verify(githubHttpClient, times(4)).getPullRequestDetailsForPullRequestNumber(anyString(), any(), any());
        final List<PullRequest> expectedResults =
                Stream.of(pr74, pr75, pr76, pr77, pr78, pr79).map(pr -> mapPullRequestDtoToDomain(pr,
                        githubAdapter.getName())).toList();
        final List<PullRequest> pullRequestList = githubAdapter.pullRequestsBytesToDomain(rawPullRequestsForRepository);
        assertThat(pullRequestList).containsAll(expectedResults);
    }


    @Test
    void should_return_empty_pull_requests_list_given_an_empty_byte_array_to_map_to_domain() {
        // Given
        final byte[] emptyBytes = new byte[0];
        final GithubProperties githubProperties = new GithubProperties();
        githubProperties.setSize(3);
        final GithubAdapter githubAdapter = new GithubAdapter(mock(GithubHttpClient.class),
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
        final PullRequest pullRequest = mapPullRequestDtoToDomain(pr80, githubPlatformName);

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

    }
}
