package fr.catlean.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.model.Repository;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import org.junit.jupiter.api.Test;

import java.util.List;

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
}
