package fr.catlean.delivery.processor.infrastructure.github.adapter.unit;

import catlean.http.cient.CatleanHttpClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import fr.catlean.delivery.processor.domain.model.Repository;
import fr.catlean.delivery.processor.infrastructure.github.adapter.GithubAdapter;
import fr.catlean.delivery.processor.infrastructure.github.adapter.client.GithubHttpClient;
import fr.catlean.delivery.processor.infrastructure.github.adapter.dto.GithubRepositoryDTO;
import fr.catlean.delivery.processor.infrastructure.github.adapter.properties.GithubProperties;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GithubAdapterPullRequestsTest extends AbstractGithubAdapterTest {


//    @Test
//    void should_get_pull_requests_given_a_repository() {
//        // Given
//        final String token = faker.pokemon().name();
//        final GithubProperties githubProperties = new GithubProperties();
//        githubProperties.setSize(3);
//        githubProperties.setToken(token);
//        final CatleanHttpClient catleanHttpClient = Mockito.mock(CatleanHttpClient.class);
//        final GithubAdapter githubAdapter = new GithubAdapter(new GithubHttpClient(catleanHttpClient, objectMapper),
//                githubProperties);
//        final Repository repository =
//                Repository.builder().organisationName(faker.name().lastName()).name(faker.name().firstName()).build();
//
//        // When
//        final GithubRepositoryDTO[] githubRepositoryStubs1 = getGithubRepositoryStubs("get_repo_for_org_page_1_size_3" +
//                ".json");
//        Mockito.when(catleanHttpClient.get(
//                "https://api.github.com/orgs/" + organisationName + "/repos?sort=name&per_page=3&page=1",
//                GithubRepositoryDTO[].class, objectMapper, Map.of("Authorization", "token " + token))).thenReturn(githubRepositoryStubs1);
//        final GithubRepositoryDTO[] githubRepositoryStubs2 = getGithubRepositoryStubs("get_repo_for_org_page_2_size_3" +
//                ".json");
//        Mockito.when(catleanHttpClient.get(
//                "https://api.github.com/orgs/" + organisationName + "/repos?sort=name&per_page=3&page=2",
//                GithubRepositoryDTO[].class, objectMapper, Map.of("Authorization", "token " + token))).thenReturn(githubRepositoryStubs2);
//        final GithubRepositoryDTO[] githubRepositoryStubs3 = getGithubRepositoryStubs("get_repo_for_org_page_3_size_3" +
//                ".json");
//        Mockito.when(catleanHttpClient.get(
//                "https://api.github.com/orgs/" + organisationName + "/repos?sort=name&per_page=3&page=3",
//                GithubRepositoryDTO[].class, objectMapper, Map.of("Authorization", "token " + token))).thenReturn(githubRepositoryStubs3);
//        final byte[] rawPullRequestsForRepository = githubAdapter.getRawPullRequestsForRepository(repository);
//
//        // Then
//    }



}
