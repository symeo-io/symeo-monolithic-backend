package fr.catlean.monolithic.backend.application.rest.api.adapter.service;

import fr.catlean.monolithic.backend.application.rest.api.adapter.properties.RepositoryRetryProperties;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.job.Job;
import fr.catlean.monolithic.backend.domain.job.runnable.CollectRepositoriesJobRunnable;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.port.in.JobFacadeAdapter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class RepositoryRetryServiceTest {

    @Test
    void should_retry_on_last_job_status_given_first_collection() throws CatleanException {
        // Given
        final JobFacadeAdapter jobFacadeAdapter = Mockito.mock(JobFacadeAdapter.class);
        final RepositoryRetryProperties retryProperties = new RepositoryRetryProperties();
        retryProperties.setMaxRetryNumber(5);
        retryProperties.setRetryTimeDelayInMillis(100);
        final RepositoryRetryService repositoryRetryService = new RepositoryRetryService(jobFacadeAdapter,
                retryProperties);
        final String jobCode = CollectRepositoriesJobRunnable.JOB_CODE;
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();

        // When
        when(jobFacadeAdapter.findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(jobCode, organization))
                .thenReturn(
                        List.of()
                )
                .thenReturn(List.of(Job.builder().organizationId(organization.getId()).status(Job.CREATED).build()))
                .thenReturn(List.of(Job.builder().organizationId(organization.getId()).status(Job.STARTED).build()))
                .thenReturn(List.of(Job.builder().organizationId(organization.getId()).status(Job.FINISHED).build()));
        repositoryRetryService.checkAndRetryOnCollectionJobs(organization);

        // Then
        verify(jobFacadeAdapter, times(4)).findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(jobCode, organization);
    }

    @Test
    void should_retry_number_max_of_retry() throws CatleanException {
        // Given
        final JobFacadeAdapter jobFacadeAdapter = Mockito.mock(JobFacadeAdapter.class);
        final RepositoryRetryProperties retryProperties = new RepositoryRetryProperties();
        retryProperties.setMaxRetryNumber(3);
        retryProperties.setRetryTimeDelayInMillis(100);
        final RepositoryRetryService repositoryRetryService = new RepositoryRetryService(jobFacadeAdapter,
                retryProperties);
        final String jobCode = CollectRepositoriesJobRunnable.JOB_CODE;
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();

        // When
        when(jobFacadeAdapter.findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(jobCode, organization))
                .thenReturn(
                        List.of()
                )
                .thenReturn(List.of(Job.builder().organizationId(organization.getId()).status(Job.CREATED).build()))
                .thenReturn(List.of(Job.builder().organizationId(organization.getId()).status(Job.STARTED).build()))
                .thenReturn(List.of(Job.builder().organizationId(organization.getId()).status(Job.STARTED).build()))
                .thenReturn(List.of(Job.builder().organizationId(organization.getId()).status(Job.STARTED).build()))
                .thenReturn(List.of(Job.builder().organizationId(organization.getId()).status(Job.FAILED).build()));
        repositoryRetryService.checkAndRetryOnCollectionJobs(organization);

        // Then
        verify(jobFacadeAdapter, times(4)).findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(jobCode, organization);
    }

    @Test
    void should_not_retry_for_job_already_ran() throws CatleanException {
        // Given
        final JobFacadeAdapter jobFacadeAdapter = Mockito.mock(JobFacadeAdapter.class);
        final RepositoryRetryProperties retryProperties = new RepositoryRetryProperties();
        retryProperties.setMaxRetryNumber(5);
        retryProperties.setRetryTimeDelayInMillis(100);
        final RepositoryRetryService repositoryRetryService = new RepositoryRetryService(jobFacadeAdapter,
                retryProperties);
        final String jobCode = CollectRepositoriesJobRunnable.JOB_CODE;
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();

        // When
        when(jobFacadeAdapter.findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(jobCode, organization))
                .thenReturn(
                        List.of(Job.builder().organizationId(organization.getId()).status(Job.FINISHED).build(),
                                Job.builder().organizationId(organization.getId()).status(Job.CREATED).build()
                        ));
        repositoryRetryService.checkAndRetryOnCollectionJobs(organization);

        // Then
        verify(jobFacadeAdapter, times(1)).findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(jobCode, organization);
    }

}
