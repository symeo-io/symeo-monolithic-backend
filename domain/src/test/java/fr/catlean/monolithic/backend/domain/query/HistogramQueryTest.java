package fr.catlean.monolithic.backend.domain.query;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.helper.DateHelper;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.TeamGoal;
import fr.catlean.monolithic.backend.domain.model.account.TeamStandard;
import fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram;
import fr.catlean.monolithic.backend.domain.model.insight.view.PullRequestView;
import fr.catlean.monolithic.backend.domain.port.in.TeamGoalFacadeAdapter;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import fr.catlean.monolithic.backend.domain.service.insights.PullRequestHistogramService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class HistogramQueryTest {

    @Test
    void should_compute_pull_request_histogram_time_limit_given_an_organization_a_team_id_an_histogram_type() throws CatleanException {
        // Given
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final TeamGoalFacadeAdapter teamGoalFacadeAdapter = mock(TeamGoalFacadeAdapter.class);
        final PullRequestHistogramService pullRequestHistogramService = mock(PullRequestHistogramService.class);
        final HistogramQuery histogramQuery = new HistogramQuery(expositionStorageAdapter, teamGoalFacadeAdapter,
                pullRequestHistogramService);
        final Organization organization = Organization.builder().build();
        final String histogramType = PullRequestHistogram.TIME_LIMIT;
        final UUID teamId = UUID.randomUUID();
        final TeamGoal teamGoal =
                TeamGoal.builder().teamId(teamId).standardCode(histogramType).id(UUID.randomUUID()).build();
        final List<PullRequestView> pullRequestViews = List.of(
                PullRequestView.builder().build(),
                PullRequestView.builder().build(),
                PullRequestView.builder().build()
        );
        final Date startDate = DateHelper.stringToDate("1992-08-12");
        final Date endDate = DateHelper.stringToDate("2022-08-12");

        // When
        when(teamGoalFacadeAdapter.getTeamGoalForTeamIdAndTeamStandard(teamId, TeamStandard.buildTimeToMerge()))
                .thenReturn(teamGoal);
        when(expositionStorageAdapter.readPullRequestsTimeToMergeViewForOrganizationAndTeam(organization, teamId))
                .thenReturn(pullRequestViews);
        histogramQuery.computePullRequestTimeToMergeHistogram(organization, teamId, startDate, endDate);

        // Then
        final ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<List<PullRequestView>> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
        final ArgumentCaptor<Organization> organizationArgumentCaptor = ArgumentCaptor.forClass(Organization.class);
        final ArgumentCaptor<TeamGoal> teamGoalArgumentCaptor = ArgumentCaptor.forClass(TeamGoal.class);
        final ArgumentCaptor<List<Date>> rangeDatesArgumentCaptor = ArgumentCaptor.forClass(List.class);
        final ArgumentCaptor<Integer> rangeArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(pullRequestHistogramService, times(1))
                .getPullRequestHistogram(stringArgumentCaptor.capture(), listArgumentCaptor.capture(),
                        organizationArgumentCaptor.capture(), teamGoalArgumentCaptor.capture(),
                        rangeDatesArgumentCaptor.capture(), rangeArgumentCaptor.capture());
        assertThat(stringArgumentCaptor.getValue()).isEqualTo(histogramType);
        assertThat(listArgumentCaptor.getValue()).isEqualTo(pullRequestViews);
        assertThat(organizationArgumentCaptor.getValue()).isEqualTo(organization);
        assertThat(teamGoalArgumentCaptor.getValue()).isEqualTo(teamGoal);
    }

    @Test
    void should_compute_pull_request_histogram_size_limit_given_an_organization_a_team_id_an_histogram_type() throws CatleanException {
        // Given
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final TeamGoalFacadeAdapter teamGoalFacadeAdapter = mock(TeamGoalFacadeAdapter.class);
        final PullRequestHistogramService pullRequestHistogramService = mock(PullRequestHistogramService.class);
        final HistogramQuery histogramQuery = new HistogramQuery(expositionStorageAdapter, teamGoalFacadeAdapter,
                pullRequestHistogramService);
        final Organization organization = Organization.builder().build();
        final String histogramType = PullRequestHistogram.SIZE_LIMIT;
        final UUID teamId = UUID.randomUUID();
        final TeamGoal teamGoal =
                TeamGoal.builder().teamId(teamId).standardCode(histogramType).id(UUID.randomUUID()).build();
        final List<PullRequestView> pullRequestViews = List.of(
                PullRequestView.builder().build(),
                PullRequestView.builder().build(),
                PullRequestView.builder().build()
        );
        final Date startDate = DateHelper.stringToDate("1992-08-12");
        final Date endDate = DateHelper.stringToDate("2022-08-12");

        // When
        when(teamGoalFacadeAdapter.getTeamGoalForTeamIdAndTeamStandard(teamId, TeamStandard.buildPullRequestSize()))
                .thenReturn(teamGoal);
        when(expositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeam(organization, teamId))
                .thenReturn(pullRequestViews);
        histogramQuery.computePullRequestSizeHistogram(organization, teamId, startDate, endDate);

        // Then
        final ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<List<PullRequestView>> listArgumentCaptor = ArgumentCaptor.forClass(List.class);
        final ArgumentCaptor<Organization> organizationArgumentCaptor = ArgumentCaptor.forClass(Organization.class);
        final ArgumentCaptor<TeamGoal> teamGoalArgumentCaptor = ArgumentCaptor.forClass(TeamGoal.class);
        final ArgumentCaptor<List<Date>> rangeDatesArgumentCaptor = ArgumentCaptor.forClass(List.class);
        final ArgumentCaptor<Integer> rangeArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(pullRequestHistogramService, times(1))
                .getPullRequestHistogram(stringArgumentCaptor.capture(), listArgumentCaptor.capture(),
                        organizationArgumentCaptor.capture(), teamGoalArgumentCaptor.capture(),
                        rangeDatesArgumentCaptor.capture(), rangeArgumentCaptor.capture());
        assertThat(stringArgumentCaptor.getValue()).isEqualTo(histogramType);
        assertThat(listArgumentCaptor.getValue()).isEqualTo(pullRequestViews);
        assertThat(organizationArgumentCaptor.getValue()).isEqualTo(organization);
        assertThat(teamGoalArgumentCaptor.getValue()).isEqualTo(teamGoal);
    }


}
