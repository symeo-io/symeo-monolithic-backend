package fr.catlean.monolithic.backend.domain.query;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.Team;
import fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import fr.catlean.monolithic.backend.domain.service.insights.PullRequestHistogramService;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class HistogramQuery {

    private final ExpositionStorageAdapter expositionStorageAdapter;

    public PullRequestHistogram computePullRequestHistogram(Organization organization, UUID teamId,
                                                            String histogramType) throws CatleanException {
        final List<PullRequest> pullRequests =
                expositionStorageAdapter.findAllPullRequestsForOrganization(organization);
        return PullRequestHistogramService.getPullRequestHistogram(histogramType, pullRequests, organization,
                Team.buildTeamAll(UUID.randomUUID()), 5);
    }
}
