package fr.catlean.monolithic.backend.domain.port.out;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;

import java.util.List;

public interface VersionControlSystemAdapter {
    byte[] getRawRepositories(String organization) throws CatleanException;

    String getName();

    List<Repository> repositoriesBytesToDomain(byte[] repositoriesBytes);

    byte[] getRawPullRequestsForRepository(Repository repository, byte[] alreadyCollectedPullRequests) throws CatleanException;

    List<PullRequest> pullRequestsBytesToDomain(byte[] bytes);
}
