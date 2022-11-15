package io.symeo.monolithic.backend.job.domain.port.out;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.github.dto.GithubBranchDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.GithubTagDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.pr.GithubCommentsDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.pr.GithubCommitsDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.pr.GithubPullRequestDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.repo.GithubRepositoryDTO;

import java.util.Date;

public interface GithubApiClientAdapter {

    GithubRepositoryDTO[] getRepositoriesForVcsOrganizationName(final String vcsOrganizationName,
                                                                final Integer page,
                                                                final Integer size) throws SymeoException;

    GithubPullRequestDTO[] getPullRequestsForRepositoryAndVcsOrganizationOrderByDescDate(final String vcsOrganizationName,
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

    GithubBranchDTO[] getBranchesForVcsOrganizationAndRepository(final String vcsOrganizationName,
                                                                 final String repositoryName,
                                                                 final Integer page,
                                                                 final Integer size) throws SymeoException;

    GithubCommitsDTO[] getCommitsForVcsOrganizationAndRepositoryAndBranchInDateRange(final String vcsOrganizationName,
                                                                                     final String repositoryName,
                                                                                     final String branchName,
                                                                                     final Date startDate,
                                                                                     final Date endDate,
                                                                                     final Integer page,
                                                                                     final Integer size) throws SymeoException;

    GithubCommitsDTO[] getCommitsForVcsOrganizationAndRepositoryAndBranch(final String vcsOrganizationName,
                                                                                     final String repositoryName,
                                                                                     final String branchName,
                                                                                     final Integer page,
                                                                                     final Integer size) throws SymeoException;



    GithubTagDTO[] getTagsForVcsOrganizationAndRepository(String vcsOrganizationName, String repositoryName) throws SymeoException;
}

