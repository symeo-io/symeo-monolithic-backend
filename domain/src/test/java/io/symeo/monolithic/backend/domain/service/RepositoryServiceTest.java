package io.symeo.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.service.platform.vcs.RepositoryService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RepositoryServiceTest {

    private final Faker faker = new Faker();

    @Test
    void should_get_repository_for_organization() {
        // Given
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final RepositoryService repositoryService = new RepositoryService(expositionStorageAdapter);
        final Organization organization = Organization.builder()
                .vcsOrganization(VcsOrganization.builder().build())
                .build();
        final List<Repository> repositories = List.of(
                Repository.builder().name(faker.name().firstName()).build(),
                Repository.builder().name(faker.name().firstName()).build(),
                Repository.builder().name(faker.name().firstName()).build()
        );

        // When
        when(expositionStorageAdapter.readRepositoriesForOrganization(organization)).thenReturn(repositories);
        final List<Repository> repositoriesForOrganization =
                repositoryService.getRepositoriesForOrganization(organization);

        // Then
        assertThat(repositoriesForOrganization).isEqualTo(repositories);
    }
}
