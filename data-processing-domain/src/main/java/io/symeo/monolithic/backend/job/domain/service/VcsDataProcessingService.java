package io.symeo.monolithic.backend.job.domain.service;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.github.GithubAdapter;
import io.symeo.monolithic.backend.job.domain.model.vcs.*;
import io.symeo.monolithic.backend.job.domain.port.out.DataProcessingExpositionStorageAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.LoaderOptions;

import java.util.Date;
import java.util.List;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.dateToString;

@AllArgsConstructor
@Slf4j
public class VcsDataProcessingService {

    private final GithubAdapter githubAdapter;
    private final DataProcessingExpositionStorageAdapter dataProcessingExpositionStorageAdapter;
    private final CycleTimeDataService cycleTimeDataService;

    public void collectRepositoriesForVcsOrganization(VcsOrganization vcsOrganization) throws SymeoException {
        LOGGER.info("Collecting repositories for {} organization.", vcsOrganization.getName());
        final List<Repository> repositoriesForVcsOrganizationName =
                githubAdapter.getRepositoriesForVcsOrganization(vcsOrganization);
        LOGGER.info("Saving {} repositories for {} organization.", repositoriesForVcsOrganizationName.size(),
                vcsOrganization.getName());
        dataProcessingExpositionStorageAdapter.saveRepositories(repositoriesForVcsOrganizationName);
        LOGGER.info("Repositories recovery for {} organization, completed.", vcsOrganization.getName());
    }

    public void collectVcsDataForRepositoryAndDateRange(final Repository repository, final Date startDate,
                                                        final Date endDate, String deployDetectionType, String pullRequestMergedOnBranchRegex,
                                                        String tagRegex, List<String> excludedBranchRegexes) throws SymeoException {
        LOGGER.info("Collecting vcsData for {} organization, {} repository, between {} and {}", repository.getVcsOrganizationName(),
                repository.getName(), dateToString(startDate), dateToString(endDate));
        LOGGER.info("Collecting pullRequests for {} organization, {} repository, between {} and {}.", repository.getVcsOrganizationName(),
                repository.getName(), dateToString(startDate), dateToString(endDate));
        final List<PullRequest> pullRequestsToComputeCycleTimes = dataProcessingExpositionStorageAdapter.savePullRequestDetailsWithLinkedComments(
                githubAdapter.getPullRequestsWithLinkedCommentsForRepositoryAndDateRange(repository, startDate, endDate)
                        .stream()
                        .filter(pullRequest -> pullRequest.getLastUpdateDate().after(startDate) && pullRequest.getLastUpdateDate().before(endDate))
                        .filter(pullRequest -> pullRequest.getStatus().equals("merge") || pullRequest.getStatus().equals("open"))
                        .toList()
        );
        LOGGER.info("Collecting cycle times for {} organization, {} repository, between {} and {}", repository.getVcsOrganizationName(),
                repository.getName(), dateToString(startDate), dateToString(endDate));
        dataProcessingExpositionStorageAdapter.saveCycleTimes(
                cycleTimeDataService.computeCycleTimesForRepository(repository, pullRequestsToComputeCycleTimes, deployDetectionType, pullRequestMergedOnBranchRegex,
                        tagRegex, excludedBranchRegexes)
        );
        LOGGER.info("vcsData recovery for {} organization, {} repository, between {} and {}, completed.", repository.getVcsOrganizationName(),
                repository.getName(), dateToString(startDate), dateToString(endDate));
    }

    public void collectNonPartialData(final Repository repository) throws SymeoException {
        final List<String> branchNames = githubAdapter.getBranches(repository).stream().map(Branch::getName).toList();
        LOGGER.info("Saving commits data for {} organization, {} repository.", repository.getVcsOrganizationName(), repository.getName());
        dataProcessingExpositionStorageAdapter.saveCommits(repository.getVcsOrganizationName(),
                githubAdapter.getCommitsForBranches(repository, branchNames)
        );
        LOGGER.info("Commits recovery for {} organization, {} repository, completed.", repository.getVcsOrganizationName(), repository.getName());
        dataProcessingExpositionStorageAdapter.saveTags(
                githubAdapter.getTags(repository)
        );
    }

    public void updateCycleTimeDataForRepositoryAndDateRange(Repository repository,
                                                             String deployDetectionType,
                                                             String pullRequestMergedOnBranchRegexes,
                                                             String tagRegex,
                                                             List<String> excludeBranchRegexes) throws SymeoException {
        LOGGER.info("Updating cycle times for {} organization, {} repository.",
                repository.getVcsOrganizationName(), repository.getName());

        final List<PullRequest> pullRequestsToComputeCycleTimes =
                dataProcessingExpositionStorageAdapter.findAllPullRequestsForRepositoryId(repository.getId());
        final List<CycleTime> updatedCycleTimes = cycleTimeDataService.computeCycleTimesForRepository(
                repository, pullRequestsToComputeCycleTimes, deployDetectionType, pullRequestMergedOnBranchRegexes,
                tagRegex, excludeBranchRegexes
        );
        dataProcessingExpositionStorageAdapter.replaceCycleTimesForRepositoryId(repository.getId(), updatedCycleTimes);
        LOGGER.info("Cycle times update for {} organization, {} repository, completed.",
                repository.getVcsOrganizationName(), repository.getName());
    }
}
