package io.symeo.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.job.Job;
import io.symeo.monolithic.backend.domain.job.runnable.CollectRepositoriesJobRunnable;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.port.out.JobStorage;
import io.symeo.monolithic.backend.domain.service.job.JobService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class JobServiceTest {

    private final static Faker faker = new Faker();

    @Test
    void find_last_jobs_for_code_and_organization_and_limit() throws SymeoException {
        // Given
        final JobStorage jobStorage = mock(JobStorage.class);
        final JobService jobService = new JobService(jobStorage);
        final UUID organizationId = UUID.randomUUID();
        final UUID teamId = UUID.randomUUID();

        // When
        jobService.findLastTwoJobsInProgressOrFinishedForVcsDataCollectionJob(organizationId, teamId);

        // Then
        final ArgumentCaptor<String> jobCodeCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<UUID> organizationIdArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        final ArgumentCaptor<UUID> teamIdArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        final ArgumentCaptor<Integer> integerArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(jobStorage, times(1)).findLastTwoJobsInProgressOrFinishedForVcsDataCollectionJob(
                organizationIdArgumentCaptor.capture(), teamIdArgumentCaptor.capture());
        assertThat(organizationIdArgumentCaptor.getValue()).isEqualTo(organizationId);
        assertThat(teamIdArgumentCaptor.getValue()).isEqualTo(teamId);
    }


    @Test
    void should_find_all_jobs_order_by_update_date_desc_given_a_code_and_organization() throws SymeoException {
        // Given
        final JobStorage jobStorage = mock(JobStorage.class);
        final JobService jobService = new JobService(jobStorage);
        final Organization organization = Organization.builder().build();

        // When
        final List<Job> allJobsByCodeAndOrganizationOrderByUpdateDate =
                jobService.findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(CollectRepositoriesJobRunnable.JOB_CODE, organization);

        // Then
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Organization> organizationArgumentCaptor = ArgumentCaptor.forClass(Organization.class);
        verify(jobStorage, times(1))
                .findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(stringArgumentCaptor.capture(),
                        organizationArgumentCaptor.capture());
        assertThat(stringArgumentCaptor.getValue()).isEqualTo(CollectRepositoriesJobRunnable.JOB_CODE);
        assertThat(organizationArgumentCaptor.getValue()).isEqualTo(organization);
    }

}
