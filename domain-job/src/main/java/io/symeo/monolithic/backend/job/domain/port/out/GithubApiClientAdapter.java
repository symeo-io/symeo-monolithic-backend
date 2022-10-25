package io.symeo.monolithic.backend.job.domain.port.out;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.vcs.github.GithubBranchDTO;
import io.symeo.monolithic.backend.job.domain.model.vcs.github.GithubTagDTO;
import io.symeo.monolithic.backend.job.domain.model.vcs.github.pr.GithubCommentsDTO;
import io.symeo.monolithic.backend.job.domain.model.vcs.github.pr.GithubCommitsDTO;
import io.symeo.monolithic.backend.job.domain.model.vcs.github.pr.GithubPullRequestDTO;
import io.symeo.monolithic.backend.job.domain.model.vcs.github.repo.GithubRepositoryDTO;

import java.util.Date;

public interface GithubApiClientAdapter {

    GithubRepositoryDTO[] getRepositoriesForOrganizationName(final String vcsOrganizationName,
                                                             final Integer page,
                                                             final Integer size) throws SymeoException;

    GithubPullRequestDTO[] getPullRequestsForRepositoryAndOrganizationOrderByDescDate(final String vcsOrganizationName,
                                                                                      final String repositoryName,
                                                                                      final Integer page,
                                                                                      final Integer size) throws SymeoException;

    GithubPullRequestDTO getPullRequestDetailsForPullRequestNumber(final String vcsOrganizationName,
                                                                   final String repositoryName,
                                                                   final Integer number) throws SymeoException;

    GithubCommitsDTO[] getCommitsForPullRequestNumber(final String vcsOrganizationName,
                                                      final String repositoryName,
                                                      final int pullRequestNumber,
                                                      final Integer page,
                                                      final Integer size) throws SymeoException;

    GithubCommentsDTO[] getCommentsForPullRequestNumber(final String vcsOrganizationName,
                                                        final String repositoryName,
                                                        final Integer pullRequestNumber,
                                                        final Integer page,
                                                        final Integer size) throws SymeoException;

    GithubBranchDTO[] getBranchesForOrganizationAndRepository(final String vcsOrganizationName,
                                                              final String repositoryName,
                                                              final Integer page,
                                                              final Integer size) throws SymeoException;

    GithubCommitsDTO[] getCommitsForOrganizationAndRepositoryAndBranchFromLastCollectionDate(final String vcsOrganizationName,
                                                                                             final String repositoryName,
                                                                                             final String branchName,
                                                                                             final Date lastCollectionDate,
                                                                                             final Integer page,
                                                                                             final Integer size) throws SymeoException;

    GithubCommitsDTO[] getCommitsForOrganizationAndRepositoryAndBranch(final String vcsOrganizationName,
                                                                       final String repositoryName,
                                                                       final String branchName,
                                                                       final Integer page,
                                                                       final Integer size) throws SymeoException;

    GithubTagDTO[] getTagsForOrganizationAndRepository(String vcsOrganizationName, String repositoryName) throws SymeoException;
}

