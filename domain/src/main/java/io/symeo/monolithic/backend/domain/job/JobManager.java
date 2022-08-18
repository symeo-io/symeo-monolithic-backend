package io.symeo.monolithic.backend.domain.job;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.port.in.JobFacadeAdapter;
import io.symeo.monolithic.backend.domain.port.out.JobStorage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executor;

import static java.util.Objects.nonNull;

@AllArgsConstructor
@Slf4j
public class JobManager implements JobFacadeAdapter {

    private final Executor executor;
    private final JobStorage jobStorage;

    public Job start(final Job job) throws SymeoException {
        LOGGER.info("Starting to create job {}", job);
        final Job jobCreated = jobStorage.createJob(job);
        LOGGER.info("Job {} created : starting the job", jobCreated);
        Job jobStarted = job.toBuilder().id(jobCreated.getId()).build().started();
        jobStorage.updateJob(jobStarted);
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
                if (nonNull(job.getNextJob())) {
                    LOGGER.info("Launching nextJob for job {}", job);
                    this.start(job.nextJob);
                }
            } catch (SymeoException e) {
                LOGGER.error("Error while running job {}", job, e);
                try {
                    jobStorage.updateJob(job.failed());
                } catch (SymeoException ex) {
                    LOGGER.error("Error while updating job {} to jobStorage", job, ex);
                }
            }

        };
    }

    @Override
    public List<Job> findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(String code, Organization organization) throws SymeoException {
        return jobStorage.findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(code, organization);
    }

    @Override
    public List<Job> findLastJobsForCodeAndOrganizationAndLimit(String jobCode, Organization organization,
                                                                int numberOfJobToFind) throws SymeoException {
        return jobStorage.findLastJobsForCodeAndOrganizationAndLimitOrderByUpdateDateDesc(jobCode, organization,
                numberOfJobToFind);
    }
}
