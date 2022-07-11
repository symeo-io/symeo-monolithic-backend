package fr.catlean.monolithic.backend.infrastructure.github.adapter.unit;

import fr.catlean.http.cient.CatleanHttpClient;
import fr.catlean.monolithic.backend.domain.model.PullRequest;
import fr.catlean.monolithic.backend.domain.model.Repository;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.GithubAdapter;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.client.GithubHttpClient;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.dto.pr.GithubPullRequestDTO;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.properties.GithubProperties;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static fr.catlean.monolithic.backend.infrastructure.github.adapter.mapper.GithubMapper.mapPullRequestDtoToDomain;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class GithubAdapterPullRequestsTest extends AbstractGithubAdapterTest {


    @Test
    void should_get_pull_requests_given_a_repository() throws IOException {
        // Given
        final String token = faker.pokemon().name();
        final GithubProperties githubProperties = new GithubProperties();
        githubProperties.setSize(3);
        githubProperties.setToken(token);
        final CatleanHttpClient catleanHttpClient = Mockito.mock(CatleanHttpClient.class);
        final GithubAdapter githubAdapter = new GithubAdapter(new GithubHttpClient(catleanHttpClient, objectMapper,
                token),
                githubProperties);
        final Repository repository =
                Repository.builder().organizationName(faker.name().lastName()).name(faker.name().firstName()).build();
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
        when(catleanHttpClient.get(
                "https://api.github.com/repos/" + repository.getOrganizationName() + "/" + repository.getName() +
                        "/pulls?sort=updated&direction=desc&state=all&per_page=3&page=1",
                GithubPullRequestDTO[].class, objectMapper, authorization)).thenReturn(githubPullRequestStubs1);
        when(catleanHttpClient.get(
                "https://api.github.com/repos/" + repository.getOrganizationName() + "/" + repository.getName() +
                        "/pulls?sort=updated&direction=desc&state=all&per_page=3&page=2",
                GithubPullRequestDTO[].class, objectMapper, authorization)).thenReturn(githubPullRequestStubs2);
        when(catleanHttpClient.get(
                "https://api.github.com/repos/" + repository.getOrganizationName() + "/" + repository.getName() +
                        "/pulls?sort=updated&direction=desc&state=all&per_page=3&page=3",
                GithubPullRequestDTO[].class, objectMapper, authorization)).thenReturn(githubPullRequestStubs3);
        // PR details by PR
        when(catleanHttpClient.get("https://api.github.com/repos/" + repository.getOrganizationName() + "/" + repository.getName() + "/pulls/74", GithubPullRequestDTO.class, objectMapper, authorization))
                .thenReturn(pr74);
        when(catleanHttpClient.get("https://api.github.com/repos/" + repository.getOrganizationName() + "/" + repository.getName() + "/pulls/75", GithubPullRequestDTO.class, objectMapper, authorization))
                .thenReturn(pr75);
        when(catleanHttpClient.get("https://api.github.com/repos/" + repository.getOrganizationName() + "/" + repository.getName() + "/pulls/76", GithubPullRequestDTO.class, objectMapper, authorization))
                .thenReturn(pr76);
        when(catleanHttpClient.get("https://api.github.com/repos/" + repository.getOrganizationName() + "/" + repository.getName() + "/pulls/77", GithubPullRequestDTO.class, objectMapper, authorization))
                .thenReturn(pr77);
        when(catleanHttpClient.get("https://api.github.com/repos/" + repository.getOrganizationName() + "/" + repository.getName() + "/pulls/78", GithubPullRequestDTO.class, objectMapper, authorization))
                .thenReturn(pr78);
        when(catleanHttpClient.get("https://api.github.com/repos/" + repository.getOrganizationName() + "/" + repository.getName() + "/pulls/79", GithubPullRequestDTO.class, objectMapper, authorization))
                .thenReturn(pr79);
        when(catleanHttpClient.get("https://api.github.com/repos/" + repository.getOrganizationName() + "/" + repository.getName() + "/pulls/80", GithubPullRequestDTO.class, objectMapper, authorization))
                .thenReturn(pr80);


        final byte[] rawPullRequestsForRepository = githubAdapter.getRawPullRequestsForRepository(repository, null);

        // Then
        verify(catleanHttpClient, times(10)).get(anyString(), any(), any(), any());
        final List<PullRequest> expectedResults = Stream.of(pr74, pr75,
                pr76, pr77,
                pr78, pr79, pr80).map(pr -> mapPullRequestDtoToDomain(pr, githubAdapter.getName())).toList();
        final List<PullRequest> pullRequestList = githubAdapter.pullRequestsBytesToDomain(rawPullRequestsForRepository);
        assertThat(pullRequestList).containsAll(expectedResults);
    }

    @Test
    void should_get_incremental_pull_requests_given_a_repository_and_already_collected_pull_requests() throws IOException {
        // Given
        final String token = faker.pokemon().name();
        final GithubProperties githubProperties = new GithubProperties();
        githubProperties.setSize(10);
        githubProperties.setToken(token);
        final CatleanHttpClient catleanHttpClient = Mockito.mock(CatleanHttpClient.class);
        final GithubAdapter githubAdapter = new GithubAdapter(new GithubHttpClient(catleanHttpClient, objectMapper,
                token),
                githubProperties);
        final Repository repository =
                Repository.builder().organizationName(faker.name().lastName()).name(faker.name().firstName()).build();
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
        when(catleanHttpClient.get(
                "https://api.github.com/repos/" + repository.getOrganizationName() + "/" + repository.getName() +
                        "/pulls?sort=updated&direction=desc&state=all&per_page=10&page=1",
                GithubPullRequestDTO[].class, objectMapper, authorization)).thenReturn(githubPullRequestStubs1);
        // PR details by PR
        when(catleanHttpClient.get("https://api.github.com/repos/" + repository.getOrganizationName() + "/" + repository.getName() + "/pulls/74", GithubPullRequestDTO.class, objectMapper, authorization))
                .thenReturn(pr74);
        when(catleanHttpClient.get("https://api.github.com/repos/" + repository.getOrganizationName() + "/" + repository.getName() + "/pulls/75", GithubPullRequestDTO.class, objectMapper, authorization))
                .thenReturn(pr75);
        when(catleanHttpClient.get("https://api.github.com/repos/" + repository.getOrganizationName() + "/" + repository.getName() + "/pulls/76", GithubPullRequestDTO.class, objectMapper, authorization))
                .thenReturn(pr76);
        when(catleanHttpClient.get("https://api.github.com/repos/" + repository.getOrganizationName() + "/" + repository.getName() + "/pulls/79", GithubPullRequestDTO.class, objectMapper, authorization))
                .thenReturn(pr79);

        final byte[] rawPullRequestsForRepository = githubAdapter.getRawPullRequestsForRepository(repository,
                rawPullRequestsAlreadyCollected);

        // Then
        verify(catleanHttpClient, times(5)).get(anyString(), any(), any(), any());
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
        final String token = faker.pokemon().name();
        final GithubProperties githubProperties = new GithubProperties();
        githubProperties.setSize(3);
        githubProperties.setToken(token);
        final CatleanHttpClient catleanHttpClient = Mockito.mock(CatleanHttpClient.class);
        final GithubAdapter githubAdapter = new GithubAdapter(new GithubHttpClient(catleanHttpClient, objectMapper,
                token),
                githubProperties);

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
        assertThat(pullRequest.getState()).isEqualTo(pr80.getState());
        assertThat(pullRequest.getNumber()).isEqualTo(pr80.getNumber());
        assertThat(pullRequest.getVcsUrl()).isEqualTo(pr80.getHtmlUrl());
        assertThat(pullRequest.getTitle()).isEqualTo(pr80.getTitle());
        assertThat(pullRequest.getAuthorLogin()).isEqualTo(pr80.getUser().getLogin());

    }
}