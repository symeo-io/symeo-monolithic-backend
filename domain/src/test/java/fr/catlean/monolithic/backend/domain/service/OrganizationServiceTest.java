package fr.catlean.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import fr.catlean.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import fr.catlean.monolithic.backend.domain.service.account.OrganizationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class OrganizationServiceTest {

    private final Faker faker = new Faker();

    @Test
    void should_create_organization_given_a_vcs_organization_name_and_external_id() throws CatleanException {
        // Given
        final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter =
                Mockito.mock(AccountOrganizationStorageAdapter.class);
        final OrganizationService organizationService =
                new OrganizationService(accountOrganizationStorageAdapter);
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
    }
}
