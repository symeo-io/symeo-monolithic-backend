package fr.catlean.monolithic.backend.domain.port.out;

import fr.catlean.monolithic.backend.domain.model.PullRequest;
import fr.catlean.monolithic.backend.domain.model.Repository;

import java.util.List;

public interface VersionControlSystemAdapter {
    byte[] getRawRepositories(String organization);

    String getName();

    List<Repository> repositoriesBytesToDomain(byte[] repositoriesBytes);

    byte[] getRawPullRequestsForRepository(Repository repository, byte[] alreadyCollectedPullRequests);

    List<PullRequest> pullRequestsBytesToDomain(byte[] bytes);
}
