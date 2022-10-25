package io.symeo.monolithic.backend.job.domain.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.job.domain.model.VcsOrganization;
import io.symeo.monolithic.backend.job.domain.port.out.JobExpositionStorageAdapter;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.*;

public class JobServiceTest {
    private final static Faker faker = new Faker();

    @Test
    void should_start_repositories_collection_given_an_organization_id_and_a_vcs_organization_id() {
        // Given
        final JobExpositionStorageAdapter jobExpositionStorageAdapter = mock(JobExpositionStorageAdapter.class);
        final VcsJobService vcsJobService = mock(VcsJobService.class);
        final JobService jobService = new JobService(jobExpositionStorageAdapter, vcsJobService);
        final UUID organizationId = UUID.randomUUID();
        final UUID vcsOrganizationId = UUID.randomUUID();
        final VcsOrganization vcsOrganization = VcsOrganization.builder().build();

        // When
        when(jobExpositionStorageAdapter.findVcsOrganizationById(vcsOrganizationId))
                .thenReturn(vcsOrganization);
        jobService.startRepositoriesCollection(organizationId, vcsOrganizationId);

        // Then
        verify(vcsJobService, times(1)).collectRepositoriesForVcsOrganization(
                vcsOrganization
        );
    }


}
