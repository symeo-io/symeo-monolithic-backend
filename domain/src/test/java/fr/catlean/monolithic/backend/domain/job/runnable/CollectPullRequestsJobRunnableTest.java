package fr.catlean.monolithic.backend.domain.job.runnable;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import fr.catlean.monolithic.backend.domain.service.platform.vcs.VcsService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CollectPullRequestsJobRunnableTest {

    private final Faker faker = new Faker();

    @Test
    void should_collect_pull_requests() throws CatleanException {
        // Given
        final VcsService vcsService = mock(VcsService.class);
        final String organisationName = faker.name().username();
        Organization organisation = Organization.builder().id(UUID.randomUUID()).name(organisationName)
                .vcsOrganization(VcsOrganization.builder().build()).build();
        final CollectPullRequestsJobRunnable collectPullRequestsJobRunnable =
                new CollectPullRequestsJobRunnable(vcsService, organisation);

        // When
        collectPullRequestsJobRunnable.run();

        // Then
        verify(vcsService, times(1)).collectPullRequestsForOrganization(organisation);
    }

    @Test
    void should_raise_an_exception() throws CatleanException {
        // Given
        final VcsService vcsService = mock(VcsService.class);
        final String organisationName = faker.name().username();
        Organization organisation = Organization.builder().id(UUID.randomUUID()).name(organisationName)
                .vcsOrganization(VcsOrganization.builder().build()).build();
        final CollectPullRequestsJobRunnable collectPullRequestsJobRunnable =
                new CollectPullRequestsJobRunnable(vcsService, organisation);

        // When
        doThrow(CatleanException.class)
                .when(vcsService)
                .collectPullRequestsForOrganization(organisation);
        CatleanException catleanException = null;
        try {
            collectPullRequestsJobRunnable.run();
        } catch (CatleanException e) {
            catleanException = e;
        }

        // Then
        assertThat(catleanException).isNotNull();
    }
}
