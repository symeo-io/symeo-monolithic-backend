package io.symeo.monolithic.backend.job.domain.model.job.runnable;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.job.AbstractTasksRunnable;
import io.symeo.monolithic.backend.job.domain.model.job.JobRunnable;
import io.symeo.monolithic.backend.job.domain.model.job.Task;
import io.symeo.monolithic.backend.job.domain.model.job.runnable.task.RepositoriesDateRangeTask;
import io.symeo.monolithic.backend.job.domain.model.vcs.Repository;
import io.symeo.monolithic.backend.job.domain.port.out.DataProcessingJobStorage;
import io.symeo.monolithic.backend.job.domain.service.VcsDataProcessingService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Builder
@Slf4j
public class UpdateCycleTimeDataForOrganizationIdAndRepositoryIdsAndOrganizationSettingsJobRunnable extends AbstractTasksRunnable<RepositoriesDateRangeTask> implements JobRunnable {

    public static final String JOB_CODE = "UPDATE_CYCLE_TIME__DATA_FOR_REPOSITORY_IDS_AND_DATE_RANGES_JOB";

    @NonNull
    private final List<Repository> repositories;
    @NonNull
    private final VcsDataProcessingService vcsDataProcessingService;
    @NonNull
    private final DataProcessingJobStorage dataProcessingJobStorage;
    @NonNull
    private final String deployDetectionType;
    private final String pullRequestMergedOnBranchRegexes;
    private final String tagRegex;
    @NonNull
    private final List<String> excludeBranchRegexes;

    @Override
    public void run(Long jobId) throws SymeoException {
        executeAllTasks(this::updateCycleTimeDataForTask, dataProcessingJobStorage, jobId);
    }

    private void updateCycleTimeDataForTask(final RepositoriesDateRangeTask repositoryTask) throws SymeoException {
        for (Repository repository : repositoryTask.getRepositories()) {
            vcsDataProcessingService.updateCycleTimeDataForRepositoryAndDateRange(
                    repository,
                    deployDetectionType,
                    pullRequestMergedOnBranchRegexes,
                    tagRegex,
                    excludeBranchRegexes
            );
        }
    }

    @Override
    public String getCode() {
        return JOB_CODE;
    }

    @Override
    public void initializeTasks() throws SymeoException {
        final List<Task> tasks = new ArrayList<>();
        tasks.add(Task.newTaskForInput(
                RepositoriesDateRangeTask.builder()
                        .repositories(repositories)
                        .build()
        ));
        this.setTasks(tasks);

    }
}
