package io.symeo.monolithic.backend.domain.service.insights;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.insight.AverageLeadTime;
import io.symeo.monolithic.backend.domain.model.insight.LeadTime;
import io.symeo.monolithic.backend.domain.model.insight.LeadTimeMetrics;
import io.symeo.monolithic.backend.domain.model.insight.curve.LeadTimePieceCurveWithAverage;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.port.in.LeadTimeFacadeAdapter;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.getPreviousStartDateFromStartDateAndEndDate;
import static io.symeo.monolithic.backend.domain.model.insight.LeadTimeMetrics.buildFromCurrentAndPreviousLeadTimes;

@AllArgsConstructor
public class LeadTimeService implements LeadTimeFacadeAdapter {

    private final ExpositionStorageAdapter expositionStorageAdapter;

    @Override
    public Optional<LeadTimeMetrics> computeLeadTimeMetricsForTeamIdFromStartDateToEndDate(final Organization organization,
                                                                                           final UUID teamId,
                                                                                           final Date startDate,
                                                                                           final Date endDate) throws SymeoException {
        final List<PullRequestView> currentPullRequestWithCommitsViews =
                expositionStorageAdapter.readMergedPullRequestsWithCommitsForTeamIdFromStartDateToEndDate(teamId,
                        startDate
                        , endDate);
        final Optional<AverageLeadTime> currentLeadTime =
                AverageLeadTime.buildFromPullRequestWithCommitsViews(currentPullRequestWithCommitsViews);
        final Date previousStartDate = getPreviousStartDateFromStartDateAndEndDate(startDate,
                endDate, organization.getTimeZone());
        final List<PullRequestView> previousPullRequestWithCommitsViews =
                expositionStorageAdapter.readMergedPullRequestsWithCommitsForTeamIdFromStartDateToEndDate(teamId,
                        previousStartDate
                        , startDate);
        final Optional<AverageLeadTime> previousLeadTime =
                AverageLeadTime.buildFromPullRequestWithCommitsViews(previousPullRequestWithCommitsViews);
        return buildFromCurrentAndPreviousLeadTimes(currentLeadTime, previousLeadTime);
    }


    @Override
    public LeadTimePieceCurveWithAverage computeLeadTimeCurvesForTeamIdFromStartDateAndEndDate(final UUID teamId,
                                                                                               final Date startDate,
                                                                                               final Date endDate) throws SymeoException {
        final List<PullRequestView> currentPullRequestWithCommitsViews =
                expositionStorageAdapter.readMergedPullRequestsWithCommitsForTeamIdFromStartDateToEndDate(teamId,
                        startDate
                        , endDate);
        final List<LeadTime> leadTimes = currentPullRequestWithCommitsViews.stream()
                .map(LeadTime::computeLeadTimeForPullRequestView)
                .toList();
        return LeadTimePieceCurveWithAverage.buildPullRequestCurve(leadTimes);
    }
}
