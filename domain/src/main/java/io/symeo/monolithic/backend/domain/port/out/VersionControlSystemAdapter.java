package io.symeo.monolithic.backend.domain.port.out;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.platform.vcs.*;

import java.util.List;

public interface VersionControlSystemAdapter {
    byte[] getRawRepositories(String vcsOrganizationName) throws SymeoException;

    String getName();

    List<Repository> repositoriesBytesToDomain(byte[] repositoriesBytes);

    byte[] getRawPullRequestsForRepository(Repository repository, byte[] alreadyCollectedPullRequests) throws SymeoException;

    List<PullRequest> pullRequestsBytesToDomain(byte[] bytes);

    byte[] getRawCommitsForPullRequestNumber(final String vcsOrganizationName,
                                             final String repositoryName,
                                             final int pullRequestNumber) throws SymeoException;

    byte[] getRawCommitsForRepository(final String vcsOrganizationName,
                                      final String repositoryName,
                                      final byte[] alreadyCollectedCommits) throws SymeoException;

    List<Commit> commitsBytesToDomain(byte[] rawCommits);

    List<Comment> commentsBytesToDomain(byte[] rawComments);

    byte[] getRawComments(String vcsOrganizationName, String name, Integer number) throws SymeoException;

    byte[] getRawBranches(String vcsOrganizationName, String repositoryName) throws SymeoException;

    List<Branch> branchesBytesToDomain(byte[] rawBranches);
}
