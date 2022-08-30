package io.symeo.monolithic.backend.domain.service;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.service.insights.LeadTimeService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.getPreviousStartDateFromStartDateAndEndDate;
import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.mockito.Mockito.*;

public class AverageLeadTimeServiceTest {


    @Test
    void should_compute_lead_time_from_pull_requests_and_commits() throws SymeoException {
        // Given
        final ExpositionStorageAdapter expositionStorageAdapter = Mockito.mock(ExpositionStorageAdapter.class);
        final LeadTimeService leadTimeService = new LeadTimeService(expositionStorageAdapter);
        final List<PullRequestView> pullRequestWithCommitsViews =
                List.of(PullRequestView.builder().build());
        final UUID teamId = UUID.randomUUID();
        final Date startDate = stringToDate("2022-01-01 12:00:00");
        final Date endDate = stringToDate("2022-01-03 12:00:00");
        final Organization organization = Organization.builder().build();

        // When
        when(expositionStorageAdapter.readMergedPullRequestsWithCommitsForTeamIdFromStartDateToEndDate(teamId,
                startDate, endDate))
                .thenReturn(
                        pullRequestWithCommitsViews
                );
        leadTimeService.computeLeadTimeMetricsForTeamIdFromStartDateToEndDate(organization, teamId, startDate
                , endDate);

        // Then
        verify(expositionStorageAdapter, times(1))
                .readMergedPullRequestsWithCommitsForTeamIdFromStartDateToEndDate(teamId,
                        startDate, endDate);
        final Date previousStartDate = getPreviousStartDateFromStartDateAndEndDate(startDate,
                endDate, organization.getTimeZone());
        verify(expositionStorageAdapter, times(1))
                .readMergedPullRequestsWithCommitsForTeamIdFromStartDateToEndDate(teamId, previousStartDate, startDate);

    }
}
