package fr.catlean.monolithic.backend.domain.query;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.insight.curve.PieceCurveWithAverage;
import fr.catlean.monolithic.backend.domain.model.insight.view.PullRequestTimeToMergeView;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import lombok.AllArgsConstructor;

import java.util.List;

import static fr.catlean.monolithic.backend.domain.model.insight.curve.PieceCurveWithAverage.buildTimeToMergeCurve;

@AllArgsConstructor
public class CurveQuery {
    private final ExpositionStorageAdapter expositionStorageAdapter;

    public PieceCurveWithAverage computeTimeToMergeCurve(Organization organization, String teamName) throws CatleanException {
        final List<PullRequestTimeToMergeView> pullRequestTimeToMergeViews =
                expositionStorageAdapter.readPullRequestsTimeToMergeViewForOrganizationAndTeam(organization, teamName);
        return buildTimeToMergeCurve(pullRequestTimeToMergeViews);
    }
}
