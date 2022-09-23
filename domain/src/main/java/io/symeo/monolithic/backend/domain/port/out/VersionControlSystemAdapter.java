package io.symeo.monolithic.backend.domain.port.out;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;

import java.util.Date;
import java.util.List;

public interface VersionControlSystemAdapter {
    byte[] getRawRepositories(String vcsOrganizationName) throws SymeoException;

    String getName();

    List<Repository> repositoriesBytesToDomain(byte[] repositoriesBytes) throws SymeoException;

    byte[] getRawPullRequestsForRepository(Repository repository, byte[] alreadyCollectedPullRequests) throws SymeoException;

    List<PullRequest> pullRequestsBytesToDomain(byte[] bytes) throws SymeoException;

    List<Commit> commitsBytesToDomain(byte[] rawCommits) throws SymeoException;

    byte[] getRawCommitsForRepositoryFromLastCollectionDate(String vcsOrganizationName, String repositoryName,
                                                            Date lastCollectionDate,
                                                            byte[] alreadyCollectedCommits) throws SymeoException;
}
