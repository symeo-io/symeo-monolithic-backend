package io.symeo.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import io.symeo.monolithic.backend.domain.port.in.DataProcessingJobAdapter;
import io.symeo.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import io.symeo.monolithic.backend.domain.service.account.OrganizationService;
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
        final DataProcessingJobAdapter dataProcessingJobAdapter = mock(DataProcessingJobAdapter.class);

        final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter =
                mock(AccountOrganizationStorageAdapter.class);
        final OrganizationService organizationService =
                new OrganizationService(accountOrganizationStorageAdapter, dataProcessingJobAdapter);
        final String externalId = faker.name().name();
        final String vcsOrganizationName = faker.gameOfThrones().character();
        final Organization expectedOrganization = Organization.builder().id(UUID.randomUUID())
                .name(vcsOrganizationName)
                .vcsOrganization(
                        VcsOrganization.builder()
                                .externalId(externalId)
                                .name(vcsOrganizationName)
                                .build()
                )
                .build();

        // When
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        final Organization organization = Organization.builder()
                .name(vcsOrganizationName)
                .vcsOrganization(
                        VcsOrganization.builder()
                                .externalId(externalId)
                                .name(vcsOrganizationName)
                                .build()
                )
                .build();
        when(accountOrganizationStorageAdapter.createOrganization(organization)).thenReturn(
                expectedOrganization
        );
        final Organization result = organizationService.createOrganization(organization);

        // Then
        assertThat(result).isEqualTo(expectedOrganization);
        verify(dataProcessingJobAdapter, times(1)).start(stringArgumentCaptor.capture());
        assertThat(stringArgumentCaptor.getValue()).isEqualTo(organization.getVcsOrganization().getName());
    }
}
