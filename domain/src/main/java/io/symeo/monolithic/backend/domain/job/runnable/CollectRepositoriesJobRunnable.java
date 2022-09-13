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
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@AllArgsConstructor
@Value
@Builder
@Slf4j
public class CollectRepositoriesJobRunnable implements JobRunnable {

    VcsService vcsService;
    Organization organization;
    RepositoryService repositoryService;

    public static final String JOB_CODE = "COLLECT_REPOSITORIES_FOR_ORGANIZATION_JOB";

    @Override
    public void run() throws SymeoException {
        try {
            List<Repository> repositories = vcsService.collectRepositoriesForOrganization(organization);
            repositories =
                    repositories.stream()
                            .map(repository -> repository.toBuilder()
                                    .organizationId(organization.getId())
                                    .vcsOrganizationName(organization.getVcsOrganization().getName())
                                    .build()).toList();
            repositoryService.saveRepositories(repositories);
        } catch (SymeoException e) {
            LOGGER.error("Error while collecting repositories for organization {}", organization);
            throw e;
        }
    }

    @Override
    public String getCode() {
        return JOB_CODE;
    }

    @Override
    public List<Task> getTasks() {
        return null;
    }

    @Override
    public void setTasks(List<Task> tasks) {

    }
}
