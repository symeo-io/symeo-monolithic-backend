package io.symeo.monolithic.backend.domain.job.runnable;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.job.JobRunnable;
import io.symeo.monolithic.backend.domain.job.Task;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import io.symeo.monolithic.backend.domain.port.out.JobStorage;
import io.symeo.monolithic.backend.domain.service.platform.vcs.RepositoryService;
import io.symeo.monolithic.backend.domain.service.platform.vcs.VcsService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Value
@Builder
@Slf4j
public class CollectRepositoriesJobRunnable extends AbstractTasksRunnable<Organization> implements JobRunnable {

    @NonNull VcsService vcsService;
    @NonNull RepositoryService repositoryService;
    @NonNull AccountOrganizationStorageAdapter accountOrganizationStorageAdapter;
    @NonNull UUID organizationId;
    @NonNull JobStorage jobStorage;

    public static final String JOB_CODE = "COLLECT_REPOSITORIES_FOR_ORGANIZATION_JOB";

    @Override
    public void initializeTasks() throws SymeoException {
        final Organization organization =
                accountOrganizationStorageAdapter.findOrganizationById(organizationId);
        this.tasks = new ArrayList<>(List.of(
                Task.newTaskForInput(organization)
        ));
    }

    @Override
    public void run(final Long jobId) throws SymeoException {
        executeAllTasks(this::collectRepositoriesForOrganization, jobStorage, jobId);
    }

    private void collectRepositoriesForOrganization(Organization organization) throws SymeoException {
        LOGGER.info("Starting to collect repositories for organization {}", organization);
        List<Repository> repositories = vcsService.collectRepositoriesForOrganization(organization);
        repositories =
                repositories.stream()
                        .map(repository -> repository.toBuilder()
                                .organizationId(organization.getId())
                                .vcsOrganizationName(organization.getVcsOrganization().getName())
                                .build()).toList();
        repositoryService.saveRepositories(repositories);
        LOGGER.info("Repositories Collection finished for organization {}", organization);
    }

    @Override
    public String getCode() {
        return JOB_CODE;
    }
}
