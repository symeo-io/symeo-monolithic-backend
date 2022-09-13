package io.symeo.monolithic.backend.domain.job.runnable;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.job.JobRunnable;
import io.symeo.monolithic.backend.domain.job.Task;
import io.symeo.monolithic.backend.domain.model.account.Organization;
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
public class CollectCommitsJobRunnable implements JobRunnable {
    public static final String JOB_CODE = "COLLECT_COMMITS_JOB";
    VcsService vcsService;
    Organization organization;

    @Override
    public void run() throws SymeoException {
        LOGGER.info("Starting to collect commits for organization {}", organization);
        vcsService.collectCommitsForOrganization(organization);
        LOGGER.info("End of commits collection for organization {}", organization);
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
