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
    void should_find_all_failed_jobs_given_an_organization_id_and_a_team_id() throws SymeoException {
        // Given
        final JobStorage jobStorage = mock(JobStorage.class);
        final JobService jobService = new JobService(jobStorage);
        final UUID organizationId = UUID.randomUUID();
        final UUID teamId = UUID.randomUUID();

        // When
        final List<Job> failedJobs = List.of(
                Job.builder()
                        .id(4L)
                        .code(faker.dragonBall().character() + "-4")
                        .status(Job.FAILED)
                        .organizationId(organizationId)
                        .teamId(teamId)
                        .tasks(List.of())
                        .build()
        );
        when(jobStorage.findLastFailedJobsForOrganizationIdAndTeamIdForEachJobCode(organizationId, teamId))
                .thenReturn(
                        failedJobs
                );
        final List<Job> lastFailedJobsForOrganizationIdAndTeamIdForEachJobCode =
                jobService.findLastFailedJobsForOrganizationIdAndTeamIdForEachJobCode(organizationId, teamId);

        // Then
        assertThat(lastFailedJobsForOrganizationIdAndTeamIdForEachJobCode).isEqualTo(failedJobs);
    }


    @Test
    void find_last_jobs_for_code_and_organization_and_limit() throws SymeoException {
        // Given
        final JobStorage jobStorage = mock(JobStorage.class);
        final JobService jobService = new JobService(jobStorage);
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final int numberOfJobToFind = faker.number().randomDigit();
        final String jobCode = faker.ancient().god();


        // When
        jobService.findLastJobsForCodeAndOrganizationAndLimit(jobCode, organization, numberOfJobToFind);

        // Then
        final ArgumentCaptor<String> jobCodeCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Organization> organizationArgumentCaptor = ArgumentCaptor.forClass(Organization.class);
        final ArgumentCaptor<Integer> integerArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(jobStorage, times(1)).findLastJobsForCodeAndOrganizationAndLimitOrderByUpdateDateDesc(jobCodeCaptor.capture(),
                organizationArgumentCaptor.capture(), integerArgumentCaptor.capture());
        assertThat(jobCodeCaptor.getValue()).isEqualTo(jobCode);
        assertThat(organizationArgumentCaptor.getValue()).isEqualTo(organization);
        assertThat(integerArgumentCaptor.getValue()).isEqualTo(numberOfJobToFind);
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
