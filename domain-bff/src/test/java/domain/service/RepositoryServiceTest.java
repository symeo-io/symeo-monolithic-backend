package domain.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.vcs.RepositoryView;
import io.symeo.monolithic.backend.domain.bff.port.out.BffExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.bff.service.vcs.RepositoryService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RepositoryServiceTest {

    private final Faker faker = new Faker();

    @Test
    void should_get_repository_for_organization() {
        // Given
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final RepositoryService repositoryService = new RepositoryService(bffExpositionStorageAdapter);
        final Organization organization = Organization.builder()
                .vcsOrganization(Organization.VcsOrganization.builder().build())
                .build();
        final List<RepositoryView> repositories = List.of(
                RepositoryView.builder().name(faker.name().firstName()).build(),
                RepositoryView.builder().name(faker.name().firstName()).build(),
                RepositoryView.builder().name(faker.name().firstName()).build()
        );

        // When
        when(bffExpositionStorageAdapter.readRepositoriesForOrganization(organization)).thenReturn(repositories);
        final List<RepositoryView> repositoriesForOrganization =
                repositoryService.getRepositoriesForOrganization(organization);

        // Then
        assertThat(repositoriesForOrganization).isEqualTo(repositories);
    }
}
