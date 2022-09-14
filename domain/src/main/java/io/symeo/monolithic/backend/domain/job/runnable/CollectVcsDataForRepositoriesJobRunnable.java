package io.symeo.monolithic.backend.domain.job.runnable;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.job.JobRunnable;
import io.symeo.monolithic.backend.domain.job.Task;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.service.platform.vcs.VcsService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@AllArgsConstructor
@Value
@Builder
@Slf4j
public class CollectVcsDataForRepositoriesJobRunnable extends AbstractTasksRunnable<Repository> implements JobRunnable {
    VcsService vcsService;
    Organization organization;

    @Override
    public void run(final List<Task> tasks) throws SymeoException {
        executeAllTasks(this::collectVcsDataForRepository, tasks);
    }

    private void collectVcsDataForRepository(final Repository repository) {
        LOGGER.info("Starting to collect VCS data for organization {} and repository {}", organization, repository);
        vcsService.collectPullRequestsWithCommentsAndCommitsForOrganizationAndRepository(organization, repository);
        List<String> allBranches = vcsService.collectAllBranchesForOrganizationAndRepository(organization, repository);
        for (String branch : allBranches) {
            vcsService.collectCommitsForForOrganizationAndRepositoryAndBranch(organization, repository, branch);
        }
        LOGGER.info("VCS data collection finished for organization {} and repository {}", organization, repository);
    }

    public static final String JOB_CODE = "COLLECT_PULL_REQUESTS_FOR_ORGANIZATION_JOB";

    @Override
    public String getCode() {
        return JOB_CODE;
    }

}
