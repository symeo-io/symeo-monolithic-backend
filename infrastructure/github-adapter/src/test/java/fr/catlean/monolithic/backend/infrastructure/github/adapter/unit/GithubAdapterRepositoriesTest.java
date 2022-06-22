package fr.catlean.monolithic.backend.infrastructure.github.adapter.unit;

import fr.catlean.http.cient.CatleanHttpClient;
import fr.catlean.monolithic.backend.domain.model.Repository;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.GithubAdapter;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.client.GithubHttpClient;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.dto.repo.GithubRepositoryDTO;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.mapper.GithubMapper;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.properties.GithubProperties;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class GithubAdapterRepositoriesTest extends AbstractGithubAdapterTest {


    @Test
    void should_get_repositories_given_an_organization_name() throws IOException {
        // Given
        final String organizationName = faker.animal().name();
        final String token = faker.pokemon().name();
        final GithubProperties githubProperties = new GithubProperties();
        githubProperties.setSize(3);
        githubProperties.setToken(token);
        final CatleanHttpClient catleanHttpClient = Mockito.mock(CatleanHttpClient.class);
        final GithubAdapter githubAdapter = new GithubAdapter(new GithubHttpClient(catleanHttpClient, objectMapper, token),
                githubProperties);

        final GithubRepositoryDTO[] githubRepositoryStubs1 = getStubsFromClassT("get_repositories_for_org",
                "get_repo_for_org_page_1_size_3" +
                        ".json", GithubRepositoryDTO[].class);
        Mockito.when(catleanHttpClient.get(
                "https://api.github.com/orgs/" + organizationName + "/repos?sort=name&per_page=3&page=1",
                GithubRepositoryDTO[].class, objectMapper, Map.of("Authorization", "token " + token))).thenReturn(githubRepositoryStubs1);
        final GithubRepositoryDTO[] githubRepositoryStubs2 = getStubsFromClassT("get_repositories_for_org",
                "get_repo_for_org_page_2_size_3" +
                        ".json", GithubRepositoryDTO[].class);
        Mockito.when(catleanHttpClient.get(
                "https://api.github.com/orgs/" + organizationName + "/repos?sort=name&per_page=3&page=2",
                GithubRepositoryDTO[].class, objectMapper, Map.of("Authorization", "token " + token))).thenReturn(githubRepositoryStubs2);
        final GithubRepositoryDTO[] githubRepositoryStubs3 = getStubsFromClassT("get_repositories_for_org",
                "get_repo_for_org_page_3_size_3" +
                        ".json", GithubRepositoryDTO[].class);
        Mockito.when(catleanHttpClient.get(
                "https://api.github.com/orgs/" + organizationName + "/repos?sort=name&per_page=3&page=3",
                GithubRepositoryDTO[].class, objectMapper, Map.of("Authorization", "token " + token))).thenReturn(githubRepositoryStubs3);


        // When
        final byte[] rawRepositories = githubAdapter.getRawRepositories(organizationName);

        // Then
        assertThat(rawRepositories).isNotEmpty();
        assertThat(rawRepositories).isEqualTo(dtoStubsToBytes(githubRepositoryStubs1, githubRepositoryStubs2,
                githubRepositoryStubs3));

    }

    @Test
    void should_map_repositories_bytes_to_domain_repositories() throws IOException {
        // Given
        final String token = faker.pokemon().name();
        final GithubProperties githubProperties = new GithubProperties();
        githubProperties.setSize(3);
        githubProperties.setToken(token);
        final CatleanHttpClient catleanHttpClient = Mockito.mock(CatleanHttpClient.class);
        final GithubAdapter githubAdapter = new GithubAdapter(new GithubHttpClient(catleanHttpClient, objectMapper, token),
                githubProperties);
        final GithubRepositoryDTO[] githubRepositoryStubs1 = getStubsFromClassT("get_repositories_for_org",
                "get_repo_for_org_page_1_size_3" +
                        ".json", GithubRepositoryDTO[].class);

        // When
        final List<Repository> repositories =
                githubAdapter.repositoriesBytesToDomain(objectMapper.writeValueAsBytes(githubRepositoryStubs1));

        // Then
        assertThat(repositories).hasSize(3);
        assertThat(repositories.get(0).getName()).isEqualTo(githubRepositoryStubs1[0].getName());
        assertThat(repositories.get(1).getName()).isEqualTo(githubRepositoryStubs1[1].getName());
        assertThat(repositories.get(2).getName()).isEqualTo(githubRepositoryStubs1[2].getName());
    }


    @Test
    void should_map_dto_to_domain() throws IOException {
        // Given
        final GithubRepositoryDTO[] githubRepositoryStubs1 = getStubsFromClassT("get_repositories_for_org",
                "get_repo_for_org_page_1_size_3" +
                        ".json", GithubRepositoryDTO[].class);
        final GithubRepositoryDTO githubRepositoryDTO = githubRepositoryStubs1[0];


        // When
        final Repository repository = GithubMapper.mapRepositoryDtoToDomain(githubRepositoryDTO);

        // Then
        assertThat(repository.getName()).isEqualTo(githubRepositoryDTO.getName());
        assertThat(repository.getOrganizationName()).isEqualTo(githubRepositoryDTO.getOwner().getLogin());
    }
}
