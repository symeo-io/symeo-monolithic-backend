package io.symeo.monolithic.backend.job.domain.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.vcs.VcsOrganization;
import io.symeo.monolithic.backend.job.domain.port.out.AutoSymeoDataProcessingJobApiAdapter;
import io.symeo.monolithic.backend.job.domain.port.out.VcsOrganizationStorageAdapter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class OrganizationJobServiceTest {

    private static final Faker faker = new Faker();

    @Test
    void should_start_vcs_data_collection_for_all_organization() throws SymeoException {
        // Given
        final VcsOrganizationStorageAdapter vcsOrganizationStorageAdapter =
                mock(VcsOrganizationStorageAdapter.class);
        final AutoSymeoDataProcessingJobApiAdapter autoSymeoDataProcessingJobApiAdapter =
                mock(AutoSymeoDataProcessingJobApiAdapter.class);
        final OrganizationJobService organizationJobService =
                new OrganizationJobService(vcsOrganizationStorageAdapter,
                        autoSymeoDataProcessingJobApiAdapter);

        // When
        final VcsOrganization vcsOrganization1 = VcsOrganization.builder()
                .externalId(faker.pokemon().location())
                .organizationId(UUID.randomUUID())
                .id(faker.number().randomNumber())
                .vcsId(faker.pokemon().location())
                .name(faker.robin().quote())
                .build();
        final VcsOrganization vcsOrganization2 = VcsOrganization.builder()
                .externalId(faker.pokemon().name())
                .organizationId(UUID.randomUUID())
                .id(faker.number().randomNumber() + 1L)
                .vcsId(faker.pokemon().location())
                .name(faker.robin().quote())
                .build();
        when(vcsOrganizationStorageAdapter.findAllVcsOrganization())
                .thenReturn(List.of(
                        vcsOrganization1,
                        vcsOrganization2
                ));
        organizationJobService.startAll();

        // Then
        verify(autoSymeoDataProcessingJobApiAdapter, times(1))
                .autoStartDataProcessingJobForOrganizationIdAndVcsOrganizationId(vcsOrganization1.getOrganizationId()
                        , vcsOrganization1.getId());
        verify(autoSymeoDataProcessingJobApiAdapter, times(1))
                .autoStartDataProcessingJobForOrganizationIdAndVcsOrganizationId(vcsOrganization2.getOrganizationId()
                        , vcsOrganization2.getId());
    }
}
