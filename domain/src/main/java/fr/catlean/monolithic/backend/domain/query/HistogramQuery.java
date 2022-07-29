package fr.catlean.monolithic.backend.domain.query;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.TeamGoal;
import fr.catlean.monolithic.backend.domain.model.account.TeamStandard;
import fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import fr.catlean.monolithic.backend.domain.port.out.TeamGoalStorage;
import fr.catlean.monolithic.backend.domain.service.insights.PullRequestHistogramService;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

import static fr.catlean.monolithic.backend.domain.exception.CatleanExceptionCode.TEAM_NOT_FOUND;

@AllArgsConstructor
public class HistogramQuery {

    private final ExpositionStorageAdapter expositionStorageAdapter;
    private final TeamGoalStorage teamGoalStorage;
    private final PullRequestHistogramService pullRequestHistogramService;


    public PullRequestHistogram computePullRequestHistogram(Organization organization, UUID teamId,
                                                            String histogramType) throws CatleanException {
        final TeamGoal currentTeamGoal = teamGoalStorage.readForTeamId(teamId)
                .stream().filter(teamGoal -> teamGoal.getStandardCode().equals(TeamStandard.TIME_TO_MERGE)).findFirst()
                .orElseThrow(() -> CatleanException.builder()
                        .code(TEAM_NOT_FOUND)
                        .message(String.format("Team not found for id %s", teamId))
                        .build());
        final List<PullRequest> pullRequests =
                expositionStorageAdapter.findAllPullRequestsForOrganizationAndTeamId(organization, teamId);
        return pullRequestHistogramService.getPullRequestHistogram(histogramType, pullRequests, organization,
                currentTeamGoal);
    }
}
