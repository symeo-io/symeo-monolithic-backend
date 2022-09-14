package io.symeo.monolithic.backend.domain.job.runnable;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import io.symeo.monolithic.backend.domain.service.platform.vcs.VcsService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CollectVcsDataForRepositoriesJobRunnableTest {

    private final Faker faker = new Faker();

    @Test
    void should_collect_pull_requests() throws SymeoException {
        // Given
        final VcsService vcsService = mock(VcsService.class);
        final String organisationName = faker.name().username();
        Organization organisation = Organization.builder().id(UUID.randomUUID()).name(organisationName)
                .vcsOrganization(VcsOrganization.builder().build()).build();
        final CollectVcsDataForRepositoriesJobRunnable collectVcsDataForRepositoriesJobRunnable =
                new CollectVcsDataForRepositoriesJobRunnable(vcsService, organisation);

        // When
        collectVcsDataForRepositoriesJobRunnable.run(List.of());

        // Then
        verify(vcsService, times(1)).collectPullRequestsForOrganization(organisation);
    }

    @Test
    void should_raise_an_exception() throws SymeoException {
        // Given
        final VcsService vcsService = mock(VcsService.class);
        final String organisationName = faker.name().username();
        Organization organisation = Organization.builder().id(UUID.randomUUID()).name(organisationName)
                .vcsOrganization(VcsOrganization.builder().build()).build();
        final CollectVcsDataForRepositoriesJobRunnable collectVcsDataForRepositoriesJobRunnable =
                new CollectVcsDataForRepositoriesJobRunnable(vcsService, organisation);

        // When
        doThrow(SymeoException.class)
                .when(vcsService)
                .collectPullRequestsForOrganization(organisation);
        SymeoException symeoException = null;
        try {
            collectVcsDataForRepositoriesJobRunnable.run(List.of());
        } catch (SymeoException e) {
            symeoException = e;
        }

        // Then
        assertThat(symeoException).isNotNull();
    }
}
