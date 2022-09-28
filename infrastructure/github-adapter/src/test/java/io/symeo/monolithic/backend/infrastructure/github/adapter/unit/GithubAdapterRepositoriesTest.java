package io.symeo.monolithic.backend.infrastructure.github.adapter.unit;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.infrastructure.github.adapter.GithubAdapter;
import io.symeo.monolithic.backend.infrastructure.github.adapter.client.GithubHttpClient;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.repo.GithubRepositoryDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.mapper.GithubMapper;
import io.symeo.monolithic.backend.infrastructure.github.adapter.properties.GithubProperties;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class GithubAdapterRepositoriesTest extends AbstractGithubAdapterTest {


    @Test
    void should_get_repositories_given_an_organization_name() throws IOException, SymeoException {
        // Given
        final String organizationName = faker.animal().name();
        final GithubProperties githubProperties = new GithubProperties();
        githubProperties.setSize(3);
        githubProperties.setGithubAppId(faker.name().name());
        githubProperties.setPrivateKeyCertificatePath(faker.animal().name());
        final GithubHttpClient githubHttpClient = Mockito.mock(GithubHttpClient.class);
        final GithubAdapter githubAdapter = new GithubAdapter(githubHttpClient,
                githubProperties, objectMapper);

        final GithubRepositoryDTO[] githubRepositoryStubs1 = getStubsFromClassT("get_repositories_for_org",
                "get_repo_for_org_page_1_size_3" +
                        ".json", GithubRepositoryDTO[].class);
        final GithubRepositoryDTO[] githubRepositoryStubs2 = getStubsFromClassT("get_repositories_for_org",
                "get_repo_for_org_page_2_size_3" +
                        ".json", GithubRepositoryDTO[].class);
        final GithubRepositoryDTO[] githubRepositoryStubs3 = getStubsFromClassT("get_repositories_for_org",
                "get_repo_for_org_page_3_size_3" +
                        ".json", GithubRepositoryDTO[].class);
        when(githubHttpClient.getRepositoriesForOrganizationName(organizationName, 1, 3)).thenReturn(githubRepositoryStubs1);
        when(githubHttpClient.getRepositoriesForOrganizationName(organizationName, 2, 3)).thenReturn(githubRepositoryStubs2);
        when(githubHttpClient.getRepositoriesForOrganizationName(organizationName, 3, 3)).thenReturn(githubRepositoryStubs3);


        // When
        final byte[] rawRepositories = githubAdapter.getRawRepositories(organizationName);

        // Then
        assertThat(rawRepositories).isNotEmpty();
        assertThat(rawRepositories).isEqualTo(dtoStubsToBytes(githubRepositoryStubs1, githubRepositoryStubs2,
                githubRepositoryStubs3));

    }

    @Test
    void should_map_repositories_bytes_to_domain_repositories() throws IOException, SymeoException {
        // Given
        final GithubProperties githubProperties = new GithubProperties();
        githubProperties.setSize(3);
        githubProperties.setGithubAppId(faker.name().name());
        githubProperties.setPrivateKeyCertificatePath(faker.animal().name());
        final GithubAdapter githubAdapter = new GithubAdapter(Mockito.mock(GithubHttpClient.class),
                githubProperties, objectMapper);
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
        final Repository repository = GithubMapper.mapRepositoryDtoToDomain(githubRepositoryDTO, "github");

        // Then
        assertThat(repository.getName()).isEqualTo(githubRepositoryDTO.getName());
        assertThat(repository.getVcsOrganizationName()).isEqualTo(githubRepositoryDTO.getOwner().getLogin());
        assertThat(repository.getDefaultBranch()).isEqualTo((String) githubRepositoryDTO.getAdditionalProperties().get("default_branch"));
    }
}
