package io.symeo.monolithic.backend.domain.job.runnable;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.job.JobRunnable;
import io.symeo.monolithic.backend.domain.job.Task;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.service.platform.vcs.VcsService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Data
@Builder
@Slf4j
public class CollectVcsDataForOrganizationAndTeamJobRunnable extends AbstractTasksRunnable<Repository> implements JobRunnable {
    @NonNull
    final VcsService vcsService;
    Organization organization;
    @NonNull
    final ExpositionStorageAdapter expositionStorageAdapter;
    @NonNull
    final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter;
    @NonNull
    final UUID organizationId;
    @NonNull
    final UUID teamId;

    @Override
    public void initializeTasks() throws SymeoException {
        this.organization = accountOrganizationStorageAdapter.findOrganizationById(organizationId);
        final List<Repository> repositories =
                expositionStorageAdapter.findAllRepositoriesForOrganizationIdAndTeamId(organizationId, teamId);
        this.tasks = new ArrayList<>(repositories.stream()
                .map(repository -> Task.builder().input(repository).build())
                .toList());
    }

    @Override
    public void run() throws SymeoException {
        executeAllTasks(this::collectVcsDataForRepository);
    }

    private void collectVcsDataForRepository(Repository repository) throws SymeoException {
        repository = populateRepositoryWithOrganizationId(repository, organization.getId());
        LOGGER.info("Starting to collect VCS data for organization {} and repository {}", organization, repository);
        vcsService.collectPullRequestsWithCommentsAndCommitsForOrganizationAndRepository(organization, repository);
        List<String> allBranches = vcsService.collectAllBranchesForOrganizationAndRepository(organization, repository);
        for (String branch : allBranches) {
            vcsService.collectCommitsForForOrganizationAndRepositoryAndBranch(organization, repository, branch);
        }
        LOGGER.info("VCS data collection finished for organization {} and repository {}", organization, repository);
    }

    private Repository populateRepositoryWithOrganizationId(final Repository repository,
                                                            final UUID organizationId) {
        return repository.toBuilder().organizationId(organizationId).build();
    }

    public static final String JOB_CODE = "COLLECT_VCS_DATA_FOR_REPOSITORIES_JOB";

    @Override
    public String getCode() {
        return JOB_CODE;
    }

}
