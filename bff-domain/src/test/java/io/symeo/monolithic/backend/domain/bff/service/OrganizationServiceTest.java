package io.symeo.monolithic.backend.domain.bff.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.port.out.OrganizationApiKeyStorageAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.OrganizationStorageAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.BffSymeoDataProcessingJobApiAdapter;
import io.symeo.monolithic.backend.domain.bff.service.organization.OrganizationService;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class OrganizationServiceTest {

    private final Faker faker = new Faker();

    @Test
    void should_create_organization_given_a_vcs_organization_name_and_external_id() throws SymeoException {
        // Given
        final BffSymeoDataProcessingJobApiAdapter bffSymeoDataProcessingJobApiAdapter =
                mock(BffSymeoDataProcessingJobApiAdapter.class);

        final OrganizationStorageAdapter organizationStorageAdapter =
                mock(OrganizationStorageAdapter.class);
        final OrganizationApiKeyStorageAdapter organizationApiKeyStorageAdapter =
                mock(OrganizationApiKeyStorageAdapter.class);
        final OrganizationService organizationService =
                new OrganizationService(organizationStorageAdapter, organizationApiKeyStorageAdapter,
                        bffSymeoDataProcessingJobApiAdapter);
        final String externalId = faker.name().name();
        final String vcsOrganizationName = faker.gameOfThrones().character();
        final UUID organizationId = UUID.randomUUID();
        final Organization expectedOrganization = Organization.builder().id(organizationId)
                .name(vcsOrganizationName)
                .id(organizationId)
                .vcsOrganization(
                        Organization.VcsOrganization.builder()
                                .externalId(externalId)
                                .name(vcsOrganizationName)
                                .build()
                )
                .build();

        // When
        final Organization organization = Organization.builder()
                .name(vcsOrganizationName)
                .id(organizationId)
                .vcsOrganization(
                        Organization.VcsOrganization.builder()
                                .externalId(externalId)
                                .name(vcsOrganizationName)
                                .build()
                )
                .build();
        when(organizationStorageAdapter.createOrganization(organization)).thenReturn(
                expectedOrganization
        );
        final Organization result = organizationService.createOrganization(organization);

        // Then
        ArgumentCaptor<UUID> uuiDArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<Long> longDArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        assertThat(result).isEqualTo(expectedOrganization);
        verify(bffSymeoDataProcessingJobApiAdapter, times(1))
                .startDataProcessingJobForOrganizationIdAndVcsOrganizationId(uuiDArgumentCaptor.capture(),
                        longDArgumentCaptor.capture());
        assertThat(uuiDArgumentCaptor.getValue()).isEqualTo(organization.getId());
        assertThat(longDArgumentCaptor.getValue()).isEqualTo(organization.getVcsOrganization().getId());
    }
}
