package fr.catlean.monolithic.backend.domain.query;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.TeamGoal;
import fr.catlean.monolithic.backend.domain.model.account.TeamStandard;
import fr.catlean.monolithic.backend.domain.model.insight.curve.PieceCurveWithAverage;
import fr.catlean.monolithic.backend.domain.model.insight.view.PullRequestTimeToMergeView;
import fr.catlean.monolithic.backend.domain.port.in.TeamGoalFacadeAdapter;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

import static fr.catlean.monolithic.backend.domain.model.insight.curve.PieceCurveWithAverage.buildTimeToMergeCurve;

@AllArgsConstructor
public class CurveQuery {
    private final ExpositionStorageAdapter expositionStorageAdapter;
    private final TeamGoalFacadeAdapter teamGoalFacadeAdapter;

    public PieceCurveWithAverage computeTimeToMergeCurve(Organization organization, UUID teamId) throws CatleanException {
        final TeamGoal currentTeamGoal = teamGoalFacadeAdapter.getTeamGoalForTeamIdAndTeamStandard(teamId,
                TeamStandard.buildTimeToMerge());
        final List<PullRequestTimeToMergeView> pullRequestTimeToMergeViews =
                expositionStorageAdapter.readPullRequestsTimeToMergeViewForOrganizationAndTeam(organization, teamId);
        return buildTimeToMergeCurve(pullRequestTimeToMergeViews, Integer.parseInt(currentTeamGoal.getValue()));
    }
}
