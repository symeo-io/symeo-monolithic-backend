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
public class CollectVcsDataForOrganizationJobRunnable extends AbstractTasksRunnable<Repository> implements JobRunnable {
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
    final JobStorage jobStorage;

    @Override
    public void initializeTasks() throws SymeoException {
        this.organization = accountOrganizationStorageAdapter.findOrganizationById(organizationId);
        final List<Repository> repositories =
                expositionStorageAdapter.findAllRepositoriesLinkedToTeamsForOrganizationId(
                        organizationId);
        this.tasks = new ArrayList<>(repositories.stream()
                .map(repository -> Task.builder().input(repository).build())
                .toList());
    }

    @Override
    public void run(final Long jobId) throws SymeoException {
        executeAllTasks(this::collectVcsDataForRepository, jobStorage, jobId);
    }

    private void collectVcsDataForRepository(Repository repository) throws SymeoException {
        final Date lastCollectionDate =
                jobStorage.findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(this.getCode(), this.organization)
                .stream()
                .filter(job -> job.getStatus().equals(Job.FINISHED))
                .map(Job::getCreationDate)
                .findFirst()
                .orElseGet(() -> null);
        vcsService.collectVcsDataForOrganizationAndRepositoryFromLastCollectionDate(organization, repository,
                lastCollectionDate);
    }

    public static final String JOB_CODE = "COLLECT_VCS_DATA_FOR_REPOSITORIES_JOB";

    @Override
    public String getCode() {
        return JOB_CODE;
    }

}
