package io.symeo.monolithic.backend.domain.port.out;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;

import java.util.List;

public interface VersionControlSystemAdapter {
    byte[] getRawRepositories(String vcsOrganizationName) throws SymeoException;

    String getName();

    List<Repository> repositoriesBytesToDomain(byte[] repositoriesBytes);

    byte[] getRawPullRequestsForRepository(Repository repository, byte[] alreadyCollectedPullRequests) throws SymeoException;

    List<PullRequest> pullRequestsBytesToDomain(byte[] bytes);
}
