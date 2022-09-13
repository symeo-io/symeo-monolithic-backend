package io.symeo.monolithic.backend.domain.service;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.insight.LeadTime;
import io.symeo.monolithic.backend.domain.model.insight.curve.Curve;
import io.symeo.monolithic.backend.domain.model.insight.curve.LeadTimePieceCurve;
import io.symeo.monolithic.backend.domain.model.insight.curve.LeadTimePieceCurveWithAverage;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Comment;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.service.insights.LeadTimeService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void should_compute_lead_time_curves_from_pull_requests() throws SymeoException {
        // Given
        final ExpositionStorageAdapter expositionStorageAdapter = Mockito.mock(ExpositionStorageAdapter.class);
        final LeadTimeService leadTimeService = new LeadTimeService(expositionStorageAdapter);
        final List<PullRequestView> pullRequestWithCommitsViews =
                List.of(
                        PullRequestView.builder()
                                .status(PullRequest.MERGE)
                                .commits(List.of(
                                        Commit.builder().date(stringToDateTime("2022-01-01 14:00:00")).build(),
                                        Commit.builder().date(stringToDateTime("2022-01-01 15:00:00")).build()
                                ))
                                .comments(
                                        List.of(Comment.builder().creationDate(stringToDateTime("2022-01-01 17:00:00")).build())
                                )
                                .mergeDate(stringToDateTime("2022-01-01 23:00:00"))
                                .build(),
                        PullRequestView.builder()
                                .status(PullRequest.MERGE)
                                .commits(List.of(
                                        Commit.builder().date(stringToDateTime("2022-01-01 14:00:00")).build()
                                ))
                                .mergeDate(stringToDateTime("2022-01-02 23:00:00"))
                                .build(),
                        PullRequestView.builder()
                                .status(PullRequest.MERGE)
                                .commits(List.of(
                                        Commit.builder().date(stringToDateTime("2022-01-02 14:00:00")).build(),
                                        Commit.builder().date(stringToDateTime("2022-01-02 15:00:00")).build()
                                ))
                                .comments(
                                        List.of(Comment.builder().creationDate(stringToDateTime("2022-01-02 17:00:00")).build())
                                )
                                .mergeDate(stringToDateTime("2022-01-02 23:00:00"))
                                .build()
                );
        final UUID teamId = UUID.randomUUID();
        final Date startDate = stringToDateTime("2022-01-01 12:00:00");
        final Date endDate = stringToDateTime("2022-01-03 12:00:00");

        // When
        when(expositionStorageAdapter.readMergedPullRequestsWithCommitsForTeamIdFromStartDateToEndDate(teamId,
                startDate, endDate)).thenReturn(pullRequestWithCommitsViews);
        final LeadTimePieceCurveWithAverage leadTimePieceCurveWithAverage =
                leadTimeService.computeLeadTimeCurvesForTeamIdFromStartDateAndEndDate(teamId, startDate, endDate);

        // Then
        assertThat(leadTimePieceCurveWithAverage.getLeadTimePieceCurve().getData()).hasSize(3);
        final LeadTimePieceCurve.LeadTimePieceCurvePoint leadTimePieceCurvePoint =
                leadTimePieceCurveWithAverage.getLeadTimePieceCurve().getData().get(0);
        final LeadTime leadTime = LeadTime.computeLeadTimeForPullRequestView(pullRequestWithCommitsViews.get(0));
        assertThat(leadTimePieceCurvePoint.getCodingTime()).isEqualTo(leadTime.getCodingTime());
        assertThat(leadTimePieceCurvePoint.getReviewTime()).isEqualTo(leadTime.getReviewTime());
        assertThat(leadTimePieceCurvePoint.getReviewLag()).isEqualTo(leadTime.getReviewLag());
        assertThat(leadTimePieceCurvePoint.getValue()).isEqualTo(leadTime.getValue());
        assertThat(leadTimePieceCurvePoint.getDate()).isEqualTo("2022-01-01");
        assertThat(leadTimePieceCurvePoint.getLink()).isEqualTo(leadTime.getPullRequestView().getVcsUrl());
        assertThat(leadTimePieceCurvePoint.getLabel()).isEqualTo(leadTime.getPullRequestView().getBranchName());
        final List<Curve.CurvePoint> data = leadTimePieceCurveWithAverage.getAverageCurve().getData();
        assertThat(data).hasSize(2);
        assertThat(data.get(0).getDate()).isEqualTo("2022-01-01");
        assertThat(data.get(0).getValue()).isEqualTo(540f);
    }
}
