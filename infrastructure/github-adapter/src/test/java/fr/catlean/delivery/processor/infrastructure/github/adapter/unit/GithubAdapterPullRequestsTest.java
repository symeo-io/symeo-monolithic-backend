package fr.catlean.delivery.processor.infrastructure.github.adapter.unit;

import catlean.http.cient.CatleanHttpClient;
import fr.catlean.delivery.processor.domain.model.PullRequest;
import fr.catlean.delivery.processor.domain.model.Repository;
import fr.catlean.delivery.processor.infrastructure.github.adapter.GithubAdapter;
import fr.catlean.delivery.processor.infrastructure.github.adapter.client.GithubHttpClient;
import fr.catlean.delivery.processor.infrastructure.github.adapter.dto.pr.GithubPullRequestDTO;
import fr.catlean.delivery.processor.infrastructure.github.adapter.mapper.GithubMapper;
import fr.catlean.delivery.processor.infrastructure.github.adapter.properties.GithubProperties;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
        final GithubAdapter githubAdapter = new GithubAdapter(new GithubHttpClient(catleanHttpClient, objectMapper),
                githubProperties);
        final Repository repository =
                Repository.builder().organisationName(faker.name().lastName()).name(faker.name().firstName()).build();
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
                "https://api.github.com/repos/" + repository.getOrganisationName() + "/" + repository.getName() +
                        "/pulls?sort=updated&direction=desc&state=all&per_page=3&page=1",
                GithubPullRequestDTO[].class, objectMapper, authorization)).thenReturn(githubPullRequestStubs1);
        when(catleanHttpClient.get(
                "https://api.github.com/repos/" + repository.getOrganisationName() + "/" + repository.getName() +
                        "/pulls?sort=updated&direction=desc&state=all&per_page=3&page=2",
                GithubPullRequestDTO[].class, objectMapper, authorization)).thenReturn(githubPullRequestStubs2);
        when(catleanHttpClient.get(
                "https://api.github.com/repos/" + repository.getOrganisationName() + "/" + repository.getName() +
                        "/pulls?sort=updated&direction=desc&state=all&per_page=3&page=3",
                GithubPullRequestDTO[].class, objectMapper, authorization)).thenReturn(githubPullRequestStubs3);
        // PR details by PR
        when(catleanHttpClient.get("https://api.github.com/repos/" + repository.getOrganisationName() + "/" + repository.getName() + "/pulls/74", GithubPullRequestDTO.class, objectMapper, authorization))
                .thenReturn(pr74);
        when(catleanHttpClient.get("https://api.github.com/repos/" + repository.getOrganisationName() + "/" + repository.getName() + "/pulls/75", GithubPullRequestDTO.class, objectMapper, authorization))
                .thenReturn(pr75);
        when(catleanHttpClient.get("https://api.github.com/repos/" + repository.getOrganisationName() + "/" + repository.getName() + "/pulls/76", GithubPullRequestDTO.class, objectMapper, authorization))
                .thenReturn(pr76);
        when(catleanHttpClient.get("https://api.github.com/repos/" + repository.getOrganisationName() + "/" + repository.getName() + "/pulls/77", GithubPullRequestDTO.class, objectMapper, authorization))
                .thenReturn(pr77);
        when(catleanHttpClient.get("https://api.github.com/repos/" + repository.getOrganisationName() + "/" + repository.getName() + "/pulls/78", GithubPullRequestDTO.class, objectMapper, authorization))
                .thenReturn(pr78);
        when(catleanHttpClient.get("https://api.github.com/repos/" + repository.getOrganisationName() + "/" + repository.getName() + "/pulls/79", GithubPullRequestDTO.class, objectMapper, authorization))
                .thenReturn(pr79);
        when(catleanHttpClient.get("https://api.github.com/repos/" + repository.getOrganisationName() + "/" + repository.getName() + "/pulls/80", GithubPullRequestDTO.class, objectMapper, authorization))
                .thenReturn(pr80);


        final byte[] rawPullRequestsForRepository = githubAdapter.getRawPullRequestsForRepository(repository);

        // Then
        verify(catleanHttpClient, times(10)).get(anyString(), any(), any(), any());
        final List<PullRequest> expectedResults = Stream.of(pr74, pr75,
                pr76, pr77,
                pr78, pr79, pr80).map(GithubMapper::mapPullRequestDtoToDomain).toList();
        final List<PullRequest> pullRequestList = githubAdapter.pullRequestsBytesToDomain(rawPullRequestsForRepository);
        Assertions.assertThat(pullRequestList).containsAll(expectedResults);
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
        final GithubAdapter githubAdapter = new GithubAdapter(new GithubHttpClient(catleanHttpClient, objectMapper),
                githubProperties);

        // When
        final List<PullRequest> pullRequestList = githubAdapter.pullRequestsBytesToDomain(emptyBytes);

        // Then
        Assertions.assertThat(pullRequestList).isEmpty();
    }
}
