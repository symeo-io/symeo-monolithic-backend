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
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class HistogramQuery {

    private final ExpositionStorageAdapter expositionStorageAdapter;
    private final TeamGoalFacadeAdapter teamGoalFacadeAdapter;
    private final PullRequestHistogramService pullRequestHistogramService;
    private static final int RANGE = 7;

    public PullRequestHistogram computePullRequestTimeToMergeHistogram(final Organization organization,
                                                                       final UUID teamId, final Date startDate,
                                                                       final Date endDate) throws CatleanException {
        final TeamGoal currentTeamGoal = teamGoalFacadeAdapter.getTeamGoalForTeamIdAndTeamStandard(teamId,
                TeamStandard.buildTimeToMerge());
        final List<PullRequestView> pullRequests =
                expositionStorageAdapter.readPullRequestsTimeToMergeViewForOrganizationAndTeam(organization, teamId);
        final List<Date> rangeDates =
                DateHelper.getRangeDatesBetweenStartDateAndEndDateForRange(startDate, endDate, RANGE,
                        organization.getTimeZone());

        return pullRequestHistogramService.getPullRequestHistogram(PullRequestHistogram.TIME_LIMIT, pullRequests,
                organization,
                currentTeamGoal,
                rangeDates,
                RANGE);
    }

    public PullRequestHistogram computePullRequestSizeHistogram(final Organization organization, final UUID teamId,
                                                                final Date startDate, final Date endDate)
            throws CatleanException {
        final TeamGoal currentTeamGoal = teamGoalFacadeAdapter.getTeamGoalForTeamIdAndTeamStandard(teamId,
                TeamStandard.buildPullRequestSize());
        final List<PullRequestView> pullRequests =
                expositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeam(organization, teamId);
        final List<Date> rangeDates =
                DateHelper.getRangeDatesBetweenStartDateAndEndDateForRange(startDate, endDate, RANGE,
                        organization.getTimeZone());

        return pullRequestHistogramService.getPullRequestHistogram(PullRequestHistogram.SIZE_LIMIT, pullRequests,
                organization,
                currentTeamGoal,
                rangeDates,
                RANGE);
    }


}
