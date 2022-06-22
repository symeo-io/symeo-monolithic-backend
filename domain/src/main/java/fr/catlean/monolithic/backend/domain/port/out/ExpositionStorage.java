package fr.catlean.monolithic.backend.domain.port.out;

import fr.catlean.monolithic.backend.domain.model.PullRequest;
import fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram;

import java.util.List;

public interface ExpositionStorage {
    void savePullRequestDetails(List<PullRequest> pullRequests);

    void savePullRequestHistograms(List<PullRequestHistogram> pullRequestHistograms);
}
