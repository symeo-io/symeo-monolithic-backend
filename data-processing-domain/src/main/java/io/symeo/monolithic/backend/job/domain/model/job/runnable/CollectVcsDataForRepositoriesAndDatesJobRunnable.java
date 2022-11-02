package io.symeo.monolithic.backend.job.domain.model.job.runnable;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.job.AbstractTasksRunnable;
import io.symeo.monolithic.backend.job.domain.model.job.JobRunnable;
import io.symeo.monolithic.backend.job.domain.model.job.Task;
import io.symeo.monolithic.backend.job.domain.model.job.runnable.task.RepositoryDateRangeTask;
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
public class CollectVcsDataForRepositoriesAndDatesJobRunnable extends AbstractTasksRunnable<RepositoryDateRangeTask> implements JobRunnable {

    public static final String JOB_CODE = "COLLECT_VCS_DATA_FOR_REPOSITORY_IDS_AND_DATE_RANGES_JOB";
    @NonNull
    private final List<Repository> repositories;
    @NonNull
    private final VcsDataProcessingService vcsDataProcessingService;
    @NonNull
    private final DataProcessingJobStorage dataProcessingJobStorage;

    @Override
    public void run(Long jobId) throws SymeoException {
        executeAllTasks(this::collectVcsDataForTask, dataProcessingJobStorage, jobId);
    }

    private void collectVcsDataForTask(final RepositoryDateRangeTask repositoryDateRangeTask) throws SymeoException {
        LOGGER.info("Starting to collect vcs data for repositories and date range {}", repositoryDateRangeTask);
        vcsDataProcessingService.collectVcsDataForRepositoryAndDateRange(
                repositoryDateRangeTask.getRepository(),
                repositoryDateRangeTask.getStartDate(), repositoryDateRangeTask.getEndDate()
        );
        LOGGER.info("Vcs data collection finished for repositories and date range {}", repositoryDateRangeTask);
    }

    @Override
    public String getCode() {
        return JOB_CODE;
    }

    @Override
    public void initializeTasks() {
        final List<Task> tasks = new ArrayList<>();
        for (Repository repository : repositories) {
            for (List<Date> dateRange :
                    getDateRangesFromStartDateAndDateRangeNumberOfDayAndRangeNumberOfDays(
                            new Date(),
                            720, // 2 years
                            30, // 1 month
                            TimeZone.getDefault()
                    )) {
                final Date endDate = dateRange.get(0);
                final Date startDate = dateRange.get(1);
                tasks.add(Task.newTaskForInput(
                        RepositoryDateRangeTask.builder()
                                .repository(repository)
                                .startDate(startDate)
                                .endDate(endDate)
                                .build()
                ));
            }
        }
        this.setTasks(tasks);
    }

}
