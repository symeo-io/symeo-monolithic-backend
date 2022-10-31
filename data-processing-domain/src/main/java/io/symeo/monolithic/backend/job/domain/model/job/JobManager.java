package io.symeo.monolithic.backend.job.domain.model.job;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.port.out.DataProcessingJobStorage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executor;

import static java.util.Objects.nonNull;

@AllArgsConstructor
@Slf4j
public class JobManager {
    private final Executor executor;
    private final DataProcessingJobStorage dataProcessingJobStorage;

    public Job start(final Job job) throws SymeoException {
        LOGGER.info("Starting to create job {}", job);
        final Job jobCreated = dataProcessingJobStorage.createJob(job);
        LOGGER.info("Job {} created : starting the job", jobCreated);
        Job jobStarted = job.toBuilder().id(jobCreated.getId()).build().started();
        dataProcessingJobStorage.updateJob(jobStarted);
        LOGGER.info("Job {} started", jobStarted);
        executor.execute(getRunnable(jobStarted));
        return jobStarted;
    }

    public void restartFailedJobs(final List<Job> failedJobs) throws SymeoException {
        LOGGER.info("Starting to restart {} failed jobs", failedJobs.size());
        for (Job failedJob : failedJobs) {
            restartJob(failedJob);
        }
    }

    private void restartJob(Job failedJob) throws SymeoException {
        final Job restartedJob = failedJob.restarted();
        dataProcessingJobStorage.updateJob(restartedJob);
        executor.execute(getRunnable(restartedJob));
    }

    private Runnable getRunnable(final Job job) {
        return () -> {
            try {
                job.run();
                final Job jobFinished = dataProcessingJobStorage.updateJob(job.finished());
                LOGGER.info("Job {} finished", jobFinished);
                if (nonNull(job.getNextJob())) {
                    LOGGER.info("Launching nextJob for job {}", job);
                    this.start(job.getNextJob());
                }
            } catch (SymeoException symeoException) {
                LOGGER.error("Error while running job {}", job, symeoException);
                try {
                    dataProcessingJobStorage.updateJob(job.failed(symeoException));
                } catch (SymeoException ex) {
                    LOGGER.error("Error while updating job {} to jobStorage", job, ex);
                }
            }

        };
    }

}
