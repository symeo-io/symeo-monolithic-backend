package fr.catlean.monolithic.backend.domain.query;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.TeamGoal;
import fr.catlean.monolithic.backend.domain.model.account.TeamStandard;
import fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.port.in.TeamGoalFacadeAdapter;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import fr.catlean.monolithic.backend.domain.service.insights.PullRequestHistogramService;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class HistogramQuery {

    private final ExpositionStorageAdapter expositionStorageAdapter;
    private final TeamGoalFacadeAdapter teamGoalFacadeAdapter;
    private final PullRequestHistogramService pullRequestHistogramService;


    public PullRequestHistogram computePullRequestTimeToMergeHistogram(final Organization organization,
                                                                       final UUID teamId) throws CatleanException {
        final TeamGoal currentTeamGoal = teamGoalFacadeAdapter.getTeamGoalForTeamIdAndTeamStandard(teamId,
                TeamStandard.buildTimeToMerge());
        final List<PullRequest> pullRequests =
                expositionStorageAdapter.findAllPullRequestsForOrganizationAndTeamId(organization, teamId);
        return pullRequestHistogramService.getPullRequestHistogram(PullRequestHistogram.TIME_LIMIT, pullRequests,
                organization,
                currentTeamGoal);
    }
}
