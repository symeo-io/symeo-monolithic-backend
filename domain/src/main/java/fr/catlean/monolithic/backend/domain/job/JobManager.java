package fr.catlean.monolithic.backend.domain.job;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.port.out.JobStorage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;

@AllArgsConstructor
@Slf4j
public class JobManager {

    private final Executor executor;
    private final JobStorage jobStorage;

    public Job start(final Job job) throws CatleanException {
        LOGGER.info("Starting to create job {}", job);
        final Job jobCreated = jobStorage.createJob(job);
        LOGGER.info("Job {} created : starting the job", jobCreated);
        Job jobStarted = jobCreated.started();
        jobStarted = jobStorage.updateJob(jobStarted);
        LOGGER.info("Job {} started", jobStarted);
        executor.execute(getRunnable(job.getJobRunnable(), jobStarted));
        return jobStarted;
    }

    private Runnable getRunnable(final JobRunnable jobRunnable, final Job job) {
        return () -> {
            try {
                jobRunnable.run();
                final Job jobFinished = jobStorage.updateJob(job.finished());
                LOGGER.info("Job {} finished", jobFinished);
            } catch (CatleanException e) {
                LOGGER.error("Error while running job {}", job, e);
                try {
                    jobStorage.updateJob(job.failed());
                } catch (CatleanException ex) {
                    LOGGER.error("Error while updating job {} to jobStorage", job, ex);
                }
            }

        };
    }
}
