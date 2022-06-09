package fr.catlean.delivery.processor.domain.port.out;

import fr.catlean.delivery.processor.domain.model.PullRequest;
import fr.catlean.delivery.processor.domain.model.Repository;

import java.util.List;
import java.util.Optional;

public interface VersionControlSystemAdapter {
    byte[] getRawRepositories(String organisation);

    String getName();

    List<Repository> repositoriesBytesToDomain(byte[] repositoriesBytes);

    byte[] getRawPullRequestsForRepository(Repository repository, byte[] alreadyCollectedPullRequests);

    List<PullRequest> pullRequestsBytesToDomain(byte[] bytes);
}
