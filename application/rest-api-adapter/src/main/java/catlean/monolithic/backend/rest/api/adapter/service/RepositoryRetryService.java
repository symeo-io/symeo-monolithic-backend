package catlean.monolithic.backend.rest.api.adapter.service;

import catlean.monolithic.backend.rest.api.adapter.properties.RepositoryRetryProperties;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.job.Job;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.port.in.JobFacadeAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static fr.catlean.monolithic.backend.domain.exception.CatleanExceptionCode.INTERRUPTED_THREAD;
import static fr.catlean.monolithic.backend.domain.job.runnable.CollectRepositoriesJobRunnable.JOB_CODE;

@AllArgsConstructor
@Slf4j
public class RepositoryRetryService {

    private final JobFacadeAdapter jobFacadeAdapter;
    private final RepositoryRetryProperties repositoryRetryProperties;

    public void checkAndRetryOnCollectionJobs(final Organization organization) throws CatleanException {
        retry(organization, 0);
    }

    private void retry(final Organization organization, int retry) throws CatleanException {
        try {
            final List<Job> jobs =
                    jobFacadeAdapter.findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(JOB_CODE, organization);
            retry++;
            if (shouldRetry(jobs, retry)) {
                LOGGER.info("Retrying to check job {} for retry number {} and organization {}", JOB_CODE,
                        retry, organization);
                Thread.sleep(repositoryRetryProperties.getRetryTimeDelayInMillis());
                retry(organization, retry);
            }
        } catch (InterruptedException e) {
            LOGGER.error("Error while putting thread to sleep in retry", e);
            throw CatleanException.builder()
                    .code(INTERRUPTED_THREAD)
                    .message("Error while putting thread to sleep in retry")
                    .build();
        }
    }

    private boolean shouldRetry(List<Job> jobs, int retry) {
        return retry <= repositoryRetryProperties.getMaxRetryNumber()
                && (jobs.isEmpty() || (jobs.size() == 1 && !jobs.get(0).getStatus().equals(Job.FINISHED)));
    }
}
