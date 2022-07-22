package fr.catlean.monolithic.backend.domain.job.runnable;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import fr.catlean.monolithic.backend.domain.service.insights.PullRequestHistogramService;
import fr.catlean.monolithic.backend.domain.service.platform.vcs.VcsService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class CollectPullRequestsJobRunnableTest {

    private final Faker faker = new Faker();

    @Test
    void should_collect_pull_requests() throws CatleanException {
        // Given
        final VcsService vcsService = mock(VcsService.class);
        final PullRequestHistogramService pullRequestHistogramService = mock(PullRequestHistogramService.class);
        final String organisationName = faker.name().username();
        final Organization organisation = Organization.builder().id(UUID.randomUUID()).name(organisationName)
                .vcsOrganization(VcsOrganization.builder().build()).build();
        final List<PullRequest> pullRequests = List.of(PullRequest.builder().vcsId(faker.pokemon().name()).build(),
                PullRequest.builder().vcsId(faker.hacker().abbreviation()).build());
        final CollectPullRequestsJobRunnable collectPullRequestsJobRunnable =
                new CollectPullRequestsJobRunnable(vcsService, organisation, pullRequestHistogramService);

        // When
        when(vcsService.collectPullRequestsForOrganization(organisation)).thenReturn(pullRequests);
        collectPullRequestsJobRunnable.run();

        // Then
        verify(pullRequestHistogramService, times(1)).savePullRequests(pullRequests);
        verify(pullRequestHistogramService, times(1)).computeAndSavePullRequestSizeHistogram(pullRequests, organisation);
        verify(pullRequestHistogramService, times(1)).computeAndSavePullRequestTimeHistogram(pullRequests, organisation);
    }
}
