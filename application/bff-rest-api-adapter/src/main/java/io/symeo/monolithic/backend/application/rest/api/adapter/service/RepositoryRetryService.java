package io.symeo.monolithic.backend.application.rest.api.adapter.service;

import io.symeo.monolithic.backend.application.rest.api.adapter.properties.RepositoryRetryProperties;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.job.JobView;
import io.symeo.monolithic.backend.domain.bff.port.in.JobFacadeAdapter;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.INTERRUPTED_THREAD;

@AllArgsConstructor
@Slf4j
public class RepositoryRetryService {

    private final JobFacadeAdapter jobFacadeAdapter;
    private final RepositoryRetryProperties repositoryRetryProperties;

    public void checkAndRetryOnCollectionJobs(final Organization organization) throws SymeoException {
        retry(organization, 0);
    }

    private void retry(final Organization organization, int retry) throws SymeoException {
        try {
            final List<JobView> jobs =
                    jobFacadeAdapter.findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc("JOB_CODE", organization);
            retry++;
            if (shouldRetry(jobs, retry)) {
                LOGGER.info("Retrying to check job {} for retry number {} and organization {}", "JOB_CODE",
                        retry, organization);
                Thread.sleep(repositoryRetryProperties.getRetryTimeDelayInMillis());
                retry(organization, retry);
            }
        } catch (InterruptedException e) {
            LOGGER.error("Error while putting thread to sleep in retry", e);
            throw SymeoException.builder()
                    .code(INTERRUPTED_THREAD)
                    .rootException(e)
                    .message("Error while putting thread to sleep in retry")
                    .build();
        }
    }

    private boolean shouldRetry(List<JobView> jobs, int retry) {
        return retry <= repositoryRetryProperties.getMaxRetryNumber()
                && (jobs.isEmpty() || (jobs.size() == 1 && !jobs.get(0).isFinished()));
    }
}
