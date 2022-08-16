package fr.catlean.monolithic.backend.domain.query;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.helper.DateHelper;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.TeamGoal;
import fr.catlean.monolithic.backend.domain.model.account.TeamStandard;
import fr.catlean.monolithic.backend.domain.model.insight.curve.PieceCurveWithAverage;
import fr.catlean.monolithic.backend.domain.model.insight.view.PullRequestView;
import fr.catlean.monolithic.backend.domain.port.in.TeamGoalFacadeAdapter;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static fr.catlean.monolithic.backend.domain.model.insight.curve.PieceCurveWithAverage.buildPullRequestCurve;

@AllArgsConstructor
public class CurveQuery {
    private final ExpositionStorageAdapter expositionStorageAdapter;
    private final TeamGoalFacadeAdapter teamGoalFacadeAdapter;

    public PieceCurveWithAverage computeTimeToMergeCurve(final Organization organization,
                                                         final UUID teamId, final Date startDate,
                                                         final Date endDate) throws CatleanException {
        final int range = 10;
        final TeamGoal currentTeamGoal = teamGoalFacadeAdapter.getTeamGoalForTeamIdAndTeamStandard(teamId,
                TeamStandard.buildTimeToMerge());
        final List<Date> rangeDates = DateHelper.getRangeDatesBetweenStartDateAndEndDateForRange(startDate, endDate,
                range, organization.getTimeZone());
        final List<PullRequestView> pullRequestLimitViews =
                expositionStorageAdapter.readPullRequestsTimeToMergeViewForOrganizationAndTeam(organization,
                                teamId)
                        .stream()
                        .map(pullRequestLimitView -> pullRequestLimitView.addStartDateRangeFromRangeDates(rangeDates,
                                range))
                        .toList();
        return buildPullRequestCurve(pullRequestLimitViews, Integer.parseInt(currentTeamGoal.getValue()));
    }

    public PieceCurveWithAverage computePullRequestSizeCurve(final Organization organization, final UUID teamId) throws CatleanException {
        final TeamGoal currentTeamGoal = teamGoalFacadeAdapter.getTeamGoalForTeamIdAndTeamStandard(teamId,
                TeamStandard.buildPullRequestSize());
        final List<PullRequestView> pullRequestSizeViews =
                expositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeam(organization, teamId);
        return buildPullRequestCurve(pullRequestSizeViews, Integer.parseInt(currentTeamGoal.getValue()));
    }
}
