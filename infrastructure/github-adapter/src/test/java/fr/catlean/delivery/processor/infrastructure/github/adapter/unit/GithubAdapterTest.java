package fr.catlean.delivery.processor.infrastructure.github.adapter.unit;

import catlean.http.cient.CatleanHttpClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
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

import static org.assertj.core.api.Assertions.assertThat;

public class GithubAdapterTest {

    private final Faker faker = Faker.instance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void should_get_repositories_given_an_organisation_name() throws IOException {
        // Given
        final String organisationName = faker.animal().name();
        final String token = faker.pokemon().name();
        final GithubProperties githubProperties = new GithubProperties();
        githubProperties.setSize(3);
        githubProperties.setToken(token);
        final CatleanHttpClient catleanHttpClient = Mockito.mock(CatleanHttpClient.class);
        final GithubAdapter githubAdapter = new GithubAdapter(new GithubHttpClient(catleanHttpClient, objectMapper), githubProperties);

        final GithubRepositoryDTO[] githubRepositoryStubs1 = getGithubRepositoryStubs("get_repo_for_org_page_1_size_3.json");
        Mockito.when(catleanHttpClient.get(
                "https://api.github.com/orgs/" + organisationName + "/repos?sort=name&per_page=3&page=1",
                GithubRepositoryDTO[].class, objectMapper, Map.of("Authorization", "token " + token))).thenReturn(githubRepositoryStubs1);
        final GithubRepositoryDTO[] githubRepositoryStubs2 = getGithubRepositoryStubs("get_repo_for_org_page_2_size_3.json");
        Mockito.when(catleanHttpClient.get(
                "https://api.github.com/orgs/" + organisationName + "/repos?sort=name&per_page=3&page=2",
                GithubRepositoryDTO[].class, objectMapper, Map.of("Authorization", "token " + token))).thenReturn(githubRepositoryStubs2);
        final GithubRepositoryDTO[] githubRepositoryStubs3 = getGithubRepositoryStubs("get_repo_for_org_page_3_size_3.json");
        Mockito.when(catleanHttpClient.get(
                "https://api.github.com/orgs/" + organisationName + "/repos?sort=name&per_page=3&page=3",
                GithubRepositoryDTO[].class, objectMapper, Map.of("Authorization", "token " + token))).thenReturn(githubRepositoryStubs3);


        // When
        final byte[] rawRepositories = githubAdapter.getRawRepositories(organisationName);

        // Then
        assertThat(rawRepositories).isNotEmpty();
        assertThat(rawRepositories).isEqualTo(dtoStubsToBytes(githubRepositoryStubs1, githubRepositoryStubs2, githubRepositoryStubs3));

    }

    @Test
    void should_map_repositories_bytes_to_domain_repositories() throws IOException {
        // Given
        final String token = faker.pokemon().name();
        final GithubProperties githubProperties = new GithubProperties();
        githubProperties.setSize(3);
        githubProperties.setToken(token);
        final CatleanHttpClient catleanHttpClient = Mockito.mock(CatleanHttpClient.class);
        final GithubAdapter githubAdapter = new GithubAdapter(new GithubHttpClient(catleanHttpClient, objectMapper), githubProperties);
        final GithubRepositoryDTO[] githubRepositoryStubs1 = getGithubRepositoryStubs("get_repo_for_org_page_1_size_3.json");

        // When
        final List<Repository> repositories = githubAdapter.repositoriesBytesToDomain(objectMapper.writeValueAsBytes(githubRepositoryStubs1));

        // Then
        assertThat(repositories).hasSize(3);
        assertThat(repositories.get(0).getName()).isEqualTo(githubRepositoryStubs1[0].getName());
        assertThat(repositories.get(1).getName()).isEqualTo(githubRepositoryStubs1[1].getName());
        assertThat(repositories.get(2).getName()).isEqualTo(githubRepositoryStubs1[2].getName());
    }

    private GithubRepositoryDTO[] getGithubRepositoryStubs(final String fileName) throws IOException {
        final String dto1 = Files.readString(Paths.get("target/test-classes/get_repositories_for_org/" + fileName));
        return objectMapper.readValue(dto1, GithubRepositoryDTO[].class);
    }

    private byte[] dtoStubsToBytes(GithubRepositoryDTO[]... githubRepositoryDTOS) throws JsonProcessingException {
        final List<GithubRepositoryDTO> githubRepositoryDTOList = new ArrayList<>();
        for (GithubRepositoryDTO[] githubRepositoryDTO : githubRepositoryDTOS) {
            githubRepositoryDTOList.addAll(Arrays.stream(githubRepositoryDTO).toList());
        }
        return objectMapper.writeValueAsBytes(githubRepositoryDTOList.toArray());
    }

}
