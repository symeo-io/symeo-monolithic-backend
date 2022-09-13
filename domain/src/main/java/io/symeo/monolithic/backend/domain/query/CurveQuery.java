package io.symeo.monolithic.backend.domain.query;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.account.TeamGoal;
import io.symeo.monolithic.backend.domain.model.account.TeamStandard;
import io.symeo.monolithic.backend.domain.model.insight.Metrics;
import io.symeo.monolithic.backend.domain.model.insight.curve.PullRequestPieceCurveWithAverage;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.port.in.TeamGoalFacadeAdapter;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.getPreviousStartDateFromStartDateAndEndDate;
import static io.symeo.monolithic.backend.domain.helper.DateHelper.getRangeDatesBetweenStartDateAndEndDateForRange;
import static io.symeo.monolithic.backend.domain.model.insight.Metrics.buildSizeMetricsFromPullRequests;
import static io.symeo.monolithic.backend.domain.model.insight.Metrics.buildTimeToMergeMetricsFromPullRequests;
import static io.symeo.monolithic.backend.domain.model.insight.curve.PullRequestPieceCurveWithAverage.buildPullRequestCurve;

@AllArgsConstructor
public class CurveQuery {
    private final ExpositionStorageAdapter expositionStorageAdapter;
    private final TeamGoalFacadeAdapter teamGoalFacadeAdapter;

    public PullRequestPieceCurveWithAverage computeTimeToMergeCurve(final Organization organization,
                                                                    final UUID teamId, final Date startDate,
                                                                    final Date endDate) throws SymeoException {
        final TeamGoal currentTeamGoal = teamGoalFacadeAdapter.getTeamGoalForTeamIdAndTeamStandard(teamId,
                TeamStandard.buildTimeToMerge());
        final int range = 1;
        final List<Date> rangeDates = getRangeDatesBetweenStartDateAndEndDateForRange(startDate, endDate,
                range, organization.getTimeZone());
        final List<PullRequestView> pullRequestLimitViews =
                expositionStorageAdapter.readPullRequestsTimeToMergeViewForOrganizationAndTeamBetweenStartDateAndEndDate(organization,
                                teamId, startDate, endDate)
                        .stream()
                        .map(pullRequestLimitView -> pullRequestLimitView.addStartDateRangeFromRangeDates(rangeDates))
                        .map(PullRequestView::addTimeLimit)
                        .toList();
        return buildPullRequestCurve(pullRequestLimitViews, Integer.parseInt(currentTeamGoal.getValue()));
    }

    public PullRequestPieceCurveWithAverage computePullRequestSizeCurve(final Organization organization,
                                                                        final UUID teamId,
                                                                        final Date startDate, final Date endDate) throws SymeoException {
        final TeamGoal currentTeamGoal = teamGoalFacadeAdapter.getTeamGoalForTeamIdAndTeamStandard(teamId,
                TeamStandard.buildPullRequestSize());
        final int range = 1;
        final List<Date> rangeDates = getRangeDatesBetweenStartDateAndEndDateForRange(startDate, endDate,
                range, organization.getTimeZone());
        final List<PullRequestView> pullRequestSizeViews =
                expositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization,
                                teamId, startDate, endDate)
                        .stream()
                        .map(pullRequestLimitView -> pullRequestLimitView.addStartDateRangeFromRangeDates(rangeDates))
                        .map(PullRequestView::addSizeLimit)
                        .toList();
        return buildPullRequestCurve(pullRequestSizeViews, Integer.parseInt(currentTeamGoal.getValue()));
    }

    public Metrics computePullRequestSizeMetrics(Organization organization, UUID teamId, Date startDate,
                                                 Date endDate) throws SymeoException {
        final TeamGoal currentTeamGoal = teamGoalFacadeAdapter.getTeamGoalForTeamIdAndTeamStandard(teamId,
                TeamStandard.buildPullRequestSize());
        final List<PullRequestView> currentPullRequestViews =
                expositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization,
                        teamId, startDate, endDate);
        final Date previousStartDate = getPreviousStartDateFromStartDateAndEndDate(startDate,
                endDate,
                organization.getTimeZone());
        final List<PullRequestView> previousPullRequestViews =
                expositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization,
                        teamId, previousStartDate,
                        startDate);
        return buildSizeMetricsFromPullRequests(currentTeamGoal.getValueAsInteger(), endDate, startDate,
                previousStartDate, currentPullRequestViews,
                previousPullRequestViews);
    }

    public Metrics computePullRequestTimeToMergeMetrics(Organization organization, UUID teamId, Date startDate,
                                                        Date endDate) throws SymeoException {
        final TeamGoal currentTeamGoal = teamGoalFacadeAdapter.getTeamGoalForTeamIdAndTeamStandard(teamId,
                TeamStandard.buildTimeToMerge());
        final List<PullRequestView> currentPullRequestViews =
                expositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization,
                        teamId, startDate, endDate);
        final Date previousStartDate = getPreviousStartDateFromStartDateAndEndDate(startDate,
                endDate,
                organization.getTimeZone());
        final List<PullRequestView> previousPullRequestViews =
                expositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization,
                        teamId, previousStartDate,
                        startDate);
        return buildTimeToMergeMetricsFromPullRequests(currentTeamGoal.getValueAsInteger(), endDate,
                startDate, previousStartDate, currentPullRequestViews,
                previousPullRequestViews);
    }


}
