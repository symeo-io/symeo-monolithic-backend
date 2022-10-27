package io.symeo.monolithic.backend.job.domain.model.job.runnable;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.job.AbstractTasksRunnable;
import io.symeo.monolithic.backend.job.domain.model.job.JobRunnable;
import io.symeo.monolithic.backend.job.domain.model.job.Task;
import io.symeo.monolithic.backend.job.domain.model.vcs.VcsOrganization;
import io.symeo.monolithic.backend.job.domain.port.out.JobStorage;
import io.symeo.monolithic.backend.job.domain.service.VcsDataProcessingService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Builder
@Slf4j
public class CollectRepositoriesJobRunnable extends AbstractTasksRunnable<VcsOrganization> implements JobRunnable {

    public static final String JOB_CODE = "COLLECT_REPOSITORIES_FOR_VCS_ORGANIZATION_JOB";
    private final VcsDataProcessingService vcsDataProcessingService;
    private final JobStorage jobStorage;
    private final VcsOrganization vcsOrganization;

    @Override
    public void run(Long jobId) throws SymeoException {
        executeAllTasks(this::collectRepositoriesForVcsOrganization, jobStorage, jobId);
    }

    private void collectRepositoriesForVcsOrganization(VcsOrganization vcsOrganization) throws SymeoException {
        LOGGER.info("Starting to collect repositories for vcsOrganization {}", vcsOrganization);
        vcsDataProcessingService.collectRepositoriesForVcsOrganization(vcsOrganization);
        LOGGER.info("Repositories Collection finished for vcsOrganization {}", vcsOrganization);
    }

    @Override
    public String getCode() {
        return JOB_CODE;
    }

    @Override
    public void initializeTasks() {
        this.setTasks(new ArrayList<>(List.of(Task.newTaskForInput(vcsOrganization))));
    }
}
