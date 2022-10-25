package domain.query;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.TeamGoal;
import io.symeo.monolithic.backend.domain.bff.model.account.TeamStandard;
import io.symeo.monolithic.backend.domain.bff.model.metric.PullRequestHistogram;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.bff.port.in.TeamGoalFacadeAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.BffExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.bff.query.HistogramQuery;
import io.symeo.monolithic.backend.domain.bff.service.insights.PullRequestHistogramService;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.helper.DateHelper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class HistogramQueryTest {

    @Test
    void should_compute_pull_request_histogram_time_limit_given_an_organization_a_team_id_an_histogram_type() throws SymeoException {
        // Given
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final TeamGoalFacadeAdapter teamGoalFacadeAdapter = mock(TeamGoalFacadeAdapter.class);
        final PullRequestHistogramService pullRequestHistogramService = mock(PullRequestHistogramService.class);
        final HistogramQuery histogramQuery = new HistogramQuery(bffExpositionStorageAdapter, teamGoalFacadeAdapter,
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
        when(bffExpositionStorageAdapter.readPullRequestsTimeToMergeViewForOrganizationAndTeamBetweenStartDateAndEndDate(organization, teamId, startDate, endDate))
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
    void should_compute_pull_request_histogram_size_limit_given_an_organization_a_team_id_an_histogram_type() throws SymeoException {
        // Given
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final TeamGoalFacadeAdapter teamGoalFacadeAdapter = mock(TeamGoalFacadeAdapter.class);
        final PullRequestHistogramService pullRequestHistogramService = mock(PullRequestHistogramService.class);
        final HistogramQuery histogramQuery = new HistogramQuery(bffExpositionStorageAdapter, teamGoalFacadeAdapter,
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
        when(bffExpositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization, teamId, startDate, endDate))
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
