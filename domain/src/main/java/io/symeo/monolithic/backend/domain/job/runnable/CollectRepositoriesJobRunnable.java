package io.symeo.monolithic.backend.domain.job.runnable;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.job.JobRunnable;
import io.symeo.monolithic.backend.domain.job.Task;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.service.platform.vcs.RepositoryService;
import io.symeo.monolithic.backend.domain.service.platform.vcs.VcsService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
@Slf4j
public class CollectRepositoriesJobRunnable extends AbstractTasksRunnable<Organization> implements JobRunnable {

    VcsService vcsService;
    RepositoryService repositoryService;

    public static final String JOB_CODE = "COLLECT_REPOSITORIES_FOR_ORGANIZATION_JOB";

    @Override
    public void run(final List<Task> tasks) throws SymeoException {
        executeAllTasks(this::collectRepositoriesForOrganization, tasks);
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
