package io.symeo.monolithic.backend.job.domain.service;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.github.GithubAdapter;
import io.symeo.monolithic.backend.job.domain.model.vcs.Branch;
import io.symeo.monolithic.backend.job.domain.model.vcs.Repository;
import io.symeo.monolithic.backend.job.domain.model.vcs.VcsOrganization;
import io.symeo.monolithic.backend.job.domain.port.out.DataProcessingExpositionStorageAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.dateToString;

@AllArgsConstructor
@Slf4j
public class VcsDataProcessingService {

    private final GithubAdapter githubAdapter;
    private final DataProcessingExpositionStorageAdapter dataProcessingExpositionStorageAdapter;

    public void collectRepositoriesForVcsOrganization(VcsOrganization vcsOrganization) throws SymeoException {
        LOGGER.info("Starting to collect repositories for vcsOrganization {}", vcsOrganization);
        final List<Repository> repositoriesForVcsOrganizationName =
                githubAdapter.getRepositoriesForVcsOrganization(vcsOrganization);
        LOGGER.info("Saving {} repositories for vcsOrganization {}", repositoriesForVcsOrganizationName.size(),
                vcsOrganization);
        dataProcessingExpositionStorageAdapter.saveRepositories(repositoriesForVcsOrganizationName);
        LOGGER.info("{} repositories were successfully saved for vcsOrganization {}",
                repositoriesForVcsOrganizationName.size(), vcsOrganization);
    }

    public void collectVcsDataForRepositoryAndDateRange(final Repository repository, final Date startDate,
                                                        final Date endDate) throws SymeoException {
        LOGGER.info("Starting to collect vcsData for repository {} between {} and {}", repository,
                dateToString(startDate), dateToString(endDate));
        LOGGER.info("Starting to collect pullRequests for repository {} between {} and {}", repository,
                dateToString(startDate), dateToString(endDate));
        dataProcessingExpositionStorageAdapter.savePullRequestDetailsWithLinkedComments(
                githubAdapter.getPullRequestsWithLinkedCommentsForRepositoryAndDateRange(repository, startDate, endDate)
        );
    }

    public void collectNonPartialData(final Repository repository) throws SymeoException {
        final List<String> branchNames = githubAdapter.getBranches(repository).stream().map(Branch::getName).toList();
        dataProcessingExpositionStorageAdapter.saveCommits(
                githubAdapter.getCommitsForBranches(repository, branchNames)
        );
        dataProcessingExpositionStorageAdapter.saveTags(
                githubAdapter.getTags(repository)
        );
    }
}
