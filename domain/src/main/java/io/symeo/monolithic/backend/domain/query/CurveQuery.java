package io.symeo.monolithic.backend.domain.query;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.helper.DateHelper;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.account.TeamGoal;
import io.symeo.monolithic.backend.domain.model.account.TeamStandard;
import io.symeo.monolithic.backend.domain.model.insight.Metrics;
import io.symeo.monolithic.backend.domain.model.insight.curve.PieceCurveWithAverage;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.port.in.TeamGoalFacadeAdapter;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.model.insight.Metrics.buildTimeToMergeMetricsFromPullRequests;
import static io.symeo.monolithic.backend.domain.model.insight.curve.PieceCurveWithAverage.buildPullRequestCurve;

@AllArgsConstructor
public class CurveQuery {
    private final ExpositionStorageAdapter expositionStorageAdapter;
    private final TeamGoalFacadeAdapter teamGoalFacadeAdapter;

    public PieceCurveWithAverage computeTimeToMergeCurve(final Organization organization,
                                                         final UUID teamId, final Date startDate,
                                                         final Date endDate) throws SymeoException {
        final TeamGoal currentTeamGoal = teamGoalFacadeAdapter.getTeamGoalForTeamIdAndTeamStandard(teamId,
                TeamStandard.buildTimeToMerge());
        final int range = 3;
        final List<Date> rangeDates = DateHelper.getRangeDatesBetweenStartDateAndEndDateForRange(startDate, endDate,
                range, organization.getTimeZone());
        final List<PullRequestView> pullRequestLimitViews =
                expositionStorageAdapter.readPullRequestsTimeToMergeViewForOrganizationAndTeam(organization,
                                teamId)
                        .stream()
                        .map(pullRequestLimitView -> pullRequestLimitView.addStartDateRangeFromRangeDates(rangeDates))
                        .map(PullRequestView::addTimeLimit)
                        .filter(pullRequestView -> pullRequestView.isInDateRange(startDate,
                                endDate))
                        .toList();
        return buildPullRequestCurve(pullRequestLimitViews, Integer.parseInt(currentTeamGoal.getValue()));
    }

    public PieceCurveWithAverage computePullRequestSizeCurve(final Organization organization, final UUID teamId,
                                                             final Date startDate, final Date endDate) throws SymeoException {
        final TeamGoal currentTeamGoal = teamGoalFacadeAdapter.getTeamGoalForTeamIdAndTeamStandard(teamId,
                TeamStandard.buildPullRequestSize());
        final int range = 3;
        final List<Date> rangeDates = DateHelper.getRangeDatesBetweenStartDateAndEndDateForRange(startDate, endDate,
                range, organization.getTimeZone());
        final List<PullRequestView> pullRequestSizeViews =
                expositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeam(organization, teamId)
                        .stream()
                        .map(pullRequestLimitView -> pullRequestLimitView.addStartDateRangeFromRangeDates(rangeDates))
                        .map(PullRequestView::addSizeLimit)
                        .filter(pullRequestView -> pullRequestView.isInDateRange(startDate,
                                endDate))
                        .toList();
        return buildPullRequestCurve(pullRequestSizeViews, Integer.parseInt(currentTeamGoal.getValue()));
    }

    public Metrics computePullRequestSizeMetrics(Organization organization, UUID teamId, Date startDate,
                                                 Date endDate) throws SymeoException {
        final TeamGoal currentTeamGoal = teamGoalFacadeAdapter.getTeamGoalForTeamIdAndTeamStandard(teamId,
                TeamStandard.buildPullRequestSize());
        final int range = 1;
        final List<Date> currentRangeDates = DateHelper.getRangeDatesBetweenStartDateAndEndDateForRange(startDate,
                endDate,
                range, organization.getTimeZone());
        final List<Date> previousRangeDates =
                DateHelper.getPreviousRangeDateFromStartDateAndEndDate(startDate, endDate, range,
                        organization.getTimeZone());
        final List<PullRequestView> pullRequestSizeViews =
                expositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeam(organization, teamId);
        final List<PullRequestView> currentPullRequestViews = pullRequestSizeViews
                .stream()
                .map(pullRequestLimitView -> pullRequestLimitView.addStartDateRangeFromRangeDates(currentRangeDates))
                .filter(pullRequestView -> pullRequestView.isInDateRange(startDate,
                        endDate))
                .toList();
        final List<PullRequestView> previousPullRequestViews = pullRequestSizeViews
                .stream()
                .map(pullRequestLimitView -> pullRequestLimitView.addStartDateRangeFromRangeDates(previousRangeDates))
                .filter(pullRequestView -> pullRequestView.isInDateRange(DateHelper.getPreviousStartDateFromStartDateAndEndDate(startDate, endDate, organization.getTimeZone()),
                        startDate))
                .toList();
        return buildTimeToMergeMetricsFromPullRequests(currentTeamGoal.getValueAsInteger(), endDate,
                startDate, currentPullRequestViews,
                previousPullRequestViews);
    }
}
