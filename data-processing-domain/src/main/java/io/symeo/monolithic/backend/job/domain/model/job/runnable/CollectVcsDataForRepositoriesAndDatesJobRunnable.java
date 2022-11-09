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
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.getDateRangesFromStartDateAndDateRangeNumberOfDayAndRangeNumberOfDays;

@AllArgsConstructor
@Builder
@Slf4j
public class CollectVcsDataForRepositoriesAndDatesJobRunnable extends AbstractTasksRunnable<RepositoriesDateRangeTask> implements JobRunnable {

    public static final String JOB_CODE = "COLLECT_VCS_DATA_FOR_REPOSITORY_IDS_AND_DATE_RANGES_JOB";
    @NonNull
    private final List<Repository> repositories;
    @NonNull
    private final VcsDataProcessingService vcsDataProcessingService;
    @NonNull
    private final DataProcessingJobStorage dataProcessingJobStorage;
    @NonNull
    private final String deployDetectionType;
    @NonNull
    private final String pullRequestMergedOnBranchRegexes;
    @NonNull
    private final String tagRegex;
    @NonNull
    private final List<String> excludeBranchRegexes;
    @Builder.Default
    private Boolean hasCollectedNotPartialData = false;

    @Override
    public void run(Long jobId) throws SymeoException {
        executeAllTasks(this::collectVcsDataForTask, dataProcessingJobStorage, jobId);
    }

    private void collectVcsDataForTask(final RepositoriesDateRangeTask repositoriesDateRangeTask) throws SymeoException {
        for (Repository repository : repositoriesDateRangeTask.getRepositories()) {
            if (!hasCollectedNotPartialData) {
                vcsDataProcessingService.collectNonPartialData(repository);
            }
            LOGGER.info("Starting to collect vcs data for repositories and date range {}", repository);
            vcsDataProcessingService.collectVcsDataForRepositoryAndDateRange(
                    repository,
                    repositoriesDateRangeTask.getStartDate(),
                    repositoriesDateRangeTask.getEndDate(),
                    repositoriesDateRangeTask.getDeployDetectionType(),
                    repositoriesDateRangeTask.getPullRequestMergedOnBranchRegex(),
                    repositoriesDateRangeTask.getTagRegex(),
                    repositoriesDateRangeTask.getExcludedBranchRegexes()
            );
            LOGGER.info("Vcs data collection finished for repositories and date range {}", repositoriesDateRangeTask);
        }
        hasCollectedNotPartialData = true;
    }

    @Override
    public String getCode() {
        return JOB_CODE;
    }

    @Override
    public void initializeTasks() {
        final List<Task> tasks = new ArrayList<>();
        for (List<Date> dateRange :
                getDateRangesFromStartDateAndDateRangeNumberOfDayAndRangeNumberOfDays(
                        new Date(),
                        365, // 1 years
                        60, // 2 month
                        TimeZone.getDefault()
                )) {
            final Date endDate = dateRange.get(0);
            final Date startDate = dateRange.get(1);
            tasks.add(Task.newTaskForInput(
                    RepositoriesDateRangeTask.builder()
                            .repositories(repositories)
                            .startDate(startDate)
                            .endDate(endDate)
                            .deployDetectionType(deployDetectionType)
                            .pullRequestMergedOnBranchRegex(pullRequestMergedOnBranchRegexes)
                            .tagRegex(tagRegex)
                            .excludedBranchRegexes(excludeBranchRegexes)
                            .build()
            ));
        }
        tasks.sort((t1, t2) -> ((RepositoriesDateRangeTask) t2.getInput()).getStartDate()
                .compareTo(
                        ((RepositoriesDateRangeTask) t1.getInput()).getStartDate()
                ));
        this.setTasks(tasks);
    }

}
