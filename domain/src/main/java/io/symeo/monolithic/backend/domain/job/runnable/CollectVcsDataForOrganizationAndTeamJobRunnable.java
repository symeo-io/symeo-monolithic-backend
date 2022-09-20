package io.symeo.monolithic.backend.domain.job.runnable;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.job.Job;
import io.symeo.monolithic.backend.domain.job.JobRunnable;
import io.symeo.monolithic.backend.domain.job.Task;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.port.out.JobStorage;
import io.symeo.monolithic.backend.domain.service.platform.vcs.VcsService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
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
    @NonNull
    final JobStorage jobStorage;

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
    public void run(final Long jobId) throws SymeoException {
        executeAllTasks(this::collectVcsDataForRepository, jobStorage, jobId);
    }

    private void collectVcsDataForRepository(Repository repository) throws SymeoException {
        final List<Job> lastJobs =
                jobStorage.findLastJobsForCodeAndOrganizationIdAndLimitAndTeamIdOrderByUpdateDateDesc(this.getCode(),
                        this.organizationId, this.teamId, 1);
        Date lastCollectionDate = null;
        if (lastJobs.size() > 0) {
            lastCollectionDate = lastJobs.get(0).getCreationDate();
        }
        vcsService.collectVcsDataForOrganizationAndRepositoryFromLastCollectionDate(organization, repository,
                lastCollectionDate);
    }

    public static final String JOB_CODE = "COLLECT_VCS_DATA_FOR_REPOSITORIES_JOB";

    @Override
    public String getCode() {
        return JOB_CODE;
    }

}
