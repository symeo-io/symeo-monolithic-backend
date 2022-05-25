package fr.catlean.delivery.processor.infrastructure.github.adapter.unit;

import catlean.http.cient.CatleanHttpClient;
import fr.catlean.delivery.processor.domain.model.Repository;
import fr.catlean.delivery.processor.infrastructure.github.adapter.GithubAdapter;
import fr.catlean.delivery.processor.infrastructure.github.adapter.client.GithubHttpClient;
import fr.catlean.delivery.processor.infrastructure.github.adapter.dto.GithubPullRequestDTO;
import fr.catlean.delivery.processor.infrastructure.github.adapter.properties.GithubProperties;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Map;

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

        // When
        final GithubPullRequestDTO[] githubPullRequestStubs1 = getStubsFromClassT("get_pull_requests_for_repo",
                "get_pr_for_repo_page_1_size_3.json", GithubPullRequestDTO[].class);
        final GithubPullRequestDTO[] githubPullRequestStubs2 = getStubsFromClassT("get_pull_requests_for_repo",
                "get_pr_for_repo_page_2_size_3.json", GithubPullRequestDTO[].class);
        final GithubPullRequestDTO[] githubPullRequestStubs3 = getStubsFromClassT("get_pull_requests_for_repo",
                "get_pr_for_repo_page_3_size_3.json", GithubPullRequestDTO[].class);
        Mockito.when(catleanHttpClient.get(
                "https://api.github.com/repos/" + repository.getOrganisationName() + "/" + repository.getName() +
                        "/pulls?sort=updated&direction=desc&state=all&per_page=3&page=1",
                GithubPullRequestDTO[].class, objectMapper, Map.of("Authorization", "token " + token))).thenReturn(githubPullRequestStubs1);
        Mockito.when(catleanHttpClient.get(
                "https://api.github.com/repos/" + repository.getOrganisationName() + "/" + repository.getName() +
                        "/pulls?sort=updated&direction=desc&state=all&per_page=3&page=2",
                GithubPullRequestDTO[].class, objectMapper, Map.of("Authorization", "token " + token))).thenReturn(githubPullRequestStubs2);
        Mockito.when(catleanHttpClient.get(
                "https://api.github.com/repos/" + repository.getOrganisationName() + "/" + repository.getName() +
                        "/pulls?sort=updated&direction=desc&state=all&per_page=3&page=3",
                GithubPullRequestDTO[].class, objectMapper, Map.of("Authorization", "token " + token))).thenReturn(githubPullRequestStubs3);
        final byte[] rawPullRequestsForRepository = githubAdapter.getRawPullRequestsForRepository(repository);

        // Then
        Assertions.assertThat(rawPullRequestsForRepository).isEqualTo(dtoStubsToBytes(githubPullRequestStubs1,
                githubPullRequestStubs2, githubPullRequestStubs3));
    }


}
