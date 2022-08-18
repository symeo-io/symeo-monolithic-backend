package io.symeo.monolithic.backend.domain.job.runnable;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import io.symeo.monolithic.backend.domain.service.platform.vcs.RepositoryService;
import io.symeo.monolithic.backend.domain.service.platform.vcs.VcsService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CollectRepositoriesJobRunnableTest {

    private final Faker faker = new Faker();

    @Test
    void should_collect_repositories() throws SymeoException {
        // Given
        final VcsService vcsService = mock(VcsService.class);
        final RepositoryService repositoryService = mock(RepositoryService.class);
        final String vcsOrganizationId = faker.name().username();
        final Organization organisation = Organization.builder().id(UUID.randomUUID()).name(faker.name().firstName())
                .vcsOrganization(VcsOrganization.builder().build()).build();
        final List<Repository> repositories = List.of(
                Repository.builder().name(faker.name().firstName()).vcsOrganizationId(vcsOrganizationId).build(),
                Repository.builder().name(faker.name().firstName()).vcsOrganizationId(vcsOrganizationId).build(),
                Repository.builder().name(faker.name().firstName()).vcsOrganizationId(vcsOrganizationId).build()
        );
        final CollectRepositoriesJobRunnable collectRepositoriesJobRunnable =
                new CollectRepositoriesJobRunnable(vcsService, organisation, repositoryService);

        // When
        when(vcsService.collectRepositoriesForOrganization(organisation)).thenReturn(repositories);
        collectRepositoriesJobRunnable.run();

        // Then
        ArgumentCaptor<List<Repository>> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(repositoryService, times(1)).saveRepositories(listArgumentCaptor.capture());
        assertThat(listArgumentCaptor.getValue()).hasSize(repositories.size());
        listArgumentCaptor.getValue().forEach(repository -> assertThat(repository.getOrganizationId()).isNotNull());
    }

    @Test
    void should_raise_an_exception() throws SymeoException {
        // Given
        final VcsService vcsService = mock(VcsService.class);
        final RepositoryService repositoryService = mock(RepositoryService.class);
        final String organisationName = faker.name().username();
        final Organization organisation = Organization.builder().id(UUID.randomUUID()).name(organisationName)
                .vcsOrganization(VcsOrganization.builder().build()).build();
        final CollectRepositoriesJobRunnable collectRepositoriesJobRunnable =
                new CollectRepositoriesJobRunnable(vcsService, organisation, repositoryService);

        // When
        doThrow(SymeoException.class)
                .when(vcsService)
                .collectRepositoriesForOrganization(organisation);
        SymeoException symeoException = null;
        try {
            collectRepositoriesJobRunnable.run();
        } catch (SymeoException e) {
            symeoException = e;
        }
        // Then
        assertThat(symeoException).isNotNull();
    }
}
