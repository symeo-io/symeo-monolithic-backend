package fr.catlean.monolithic.backend.domain.query;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.TeamGoal;
import fr.catlean.monolithic.backend.domain.model.account.TeamStandard;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import fr.catlean.monolithic.backend.domain.port.out.TeamGoalStorage;
import fr.catlean.monolithic.backend.domain.service.insights.PullRequestHistogramService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class HistogramQueryTest {

    private final Faker faker = new Faker();

    @Test
    void should_compute_pull_request_histogram_given_an_organization_a_team_id_an_histogram_type() throws CatleanException {
        // Given
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final TeamGoalStorage teamGoalStorage = mock(TeamGoalStorage.class);
        final PullRequestHistogramService pullRequestHistogramService = mock(PullRequestHistogramService.class);
        final HistogramQuery histogramQuery = new HistogramQuery(expositionStorageAdapter, teamGoalStorage,
                pullRequestHistogramService);
        final Organization organization = Organization.builder().build();
        final String histogramType = TeamStandard.TIME_TO_MERGE;
        final UUID teamId = UUID.randomUUID();
        final TeamGoal teamGoal =
                TeamGoal.builder().teamId(teamId).standardCode(histogramType).id(UUID.randomUUID()).build();
        final List<PullRequest> pullRequests = List.of(
                PullRequest.builder().id(faker.pokemon().name()).build(),
                PullRequest.builder().id(faker.pokemon().name()).build(),
                PullRequest.builder().id(faker.pokemon().name()).build()
        );

        // When
        when(teamGoalStorage.readForTeamId(teamId))
                .thenReturn(List.of(teamGoal));
        when(expositionStorageAdapter.findAllPullRequestsForOrganizationAndTeamId(organization, teamId))
                .thenReturn(pullRequests);
        histogramQuery.computePullRequestHistogram(organization, teamId, histogramType);

        // Then
        final ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<List<PullRequest>> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
        final ArgumentCaptor<Organization> organizationArgumentCaptor = ArgumentCaptor.forClass(Organization.class);
        final ArgumentCaptor<TeamGoal> teamGoalArgumentCaptor = ArgumentCaptor.forClass(TeamGoal.class);
        verify(pullRequestHistogramService, times(1))
                .getPullRequestHistogram(stringArgumentCaptor.capture(), listArgumentCaptor.capture(),
                        organizationArgumentCaptor.capture(), teamGoalArgumentCaptor.capture());
        assertThat(stringArgumentCaptor.getValue()).isEqualTo(histogramType);
        assertThat(listArgumentCaptor.getValue()).isEqualTo(pullRequests);
        assertThat(organizationArgumentCaptor.getValue()).isEqualTo(organization);
        assertThat(teamGoalArgumentCaptor.getValue()).isEqualTo(teamGoal);
    }
}
