package io.symeo.monolithic.backend.domain.job.runnable;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.service.platform.vcs.VcsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CollectCommitsJobRunnableTest {

    @Test
    void should_collect_commits_for_organization_id() throws SymeoException {
        // Given
        final VcsService vcsService = Mockito.mock(VcsService.class);
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final CollectCommitsJobRunnable collectCommitsJobRunnable = CollectCommitsJobRunnable.builder()
                .vcsService(vcsService)
                .organization(organization)
                .build();

        // When
        collectCommitsJobRunnable.run();

        // Then
        verify(vcsService, times(1)).collectCommitsForOrganization(organization);
    }
}
