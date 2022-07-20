package fr.catlean.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.VcsConfiguration;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RepositoryServiceTest {

    private final Faker faker = new Faker();

    @Test
    void should_save_repositories() {
        // Given
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final RepositoryService repositoryService = new RepositoryService(expositionStorageAdapter);
        final List<Repository> repositories = List.of(
                Repository.builder().vcsOrganizationName(faker.name().name()).name(faker.name().firstName()).build(),
                Repository.builder().vcsOrganizationName(faker.name().name()).name(faker.name().firstName()).build(),
                Repository.builder().vcsOrganizationName(faker.name().name()).name(faker.name().firstName()).build()
        );

        // When
        repositoryService.saveRepositories(
                repositories
        );

        // Then
        verify(expositionStorageAdapter, times(1)).saveRepositories(repositories);
    }

    @Test
    void should_get_repository_for_organization() {
        // Given
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final RepositoryService repositoryService = new RepositoryService(expositionStorageAdapter);
        final Organization organization = Organization.builder()
                .vcsConfiguration(VcsConfiguration.builder().build())
                .build();
        final List<Repository> repositories = List.of(
                Repository.builder().vcsOrganizationName(faker.name().name()).name(faker.name().firstName()).build(),
                Repository.builder().vcsOrganizationName(faker.name().name()).name(faker.name().firstName()).build(),
                Repository.builder().vcsOrganizationName(faker.name().name()).name(faker.name().firstName()).build()
        );

        // When
        when(expositionStorageAdapter.readRepositoriesForOrganization(organization)).thenReturn(repositories);
        final List<Repository> repositoriesForOrganization =
                repositoryService.getRepositoriesForOrganization(organization);

        // Then
        assertThat(repositoriesForOrganization).isEqualTo(repositories);
    }
}
