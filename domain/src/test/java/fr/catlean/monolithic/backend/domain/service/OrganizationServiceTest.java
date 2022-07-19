package fr.catlean.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.VcsConfiguration;
import fr.catlean.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class OrganizationServiceTest {

    private final Faker faker = new Faker();

    // TODO : add unit test raising CatleanException
    @Test
    void should_return_organization_given_a_name() throws CatleanException {
        // Given
        final String organizationName = faker.name().firstName();
        final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter = Mockito.mock(AccountOrganizationStorageAdapter.class);
        final OrganizationService organizationService =
                new OrganizationService(accountOrganizationStorageAdapter);
        final Organization expectedOrganizationAccount =
                Organization.builder().name(organizationName).vcsConfiguration(VcsConfiguration.builder().build()).build();
        // When
        when(accountOrganizationStorageAdapter.findOrganizationForName(organizationName))
                .thenReturn(expectedOrganizationAccount);
        final Organization organizationAccount =
                organizationService.getOrganizationForName(organizationName);

        // Then
        assertThat(organizationAccount).isNotNull();
        assertThat(organizationAccount.getName()).isEqualTo(organizationName);
    }

    @Test
    void should_create_organization_given_a_vcs_organization_name_and_external_id() throws CatleanException {
        // Given
        final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter = Mockito.mock(AccountOrganizationStorageAdapter.class);
        final OrganizationService organizationService =
                new OrganizationService(accountOrganizationStorageAdapter);
        final String externalId = faker.name().name();
        final String vcsOrganizationName = faker.gameOfThrones().character();
        final Organization expectedOrganization = Organization.builder().id(UUID.randomUUID())
                .name(vcsOrganizationName)
                .externalId(externalId)
                .vcsConfiguration(
                        VcsConfiguration.builder()
                                .organizationName(vcsOrganizationName)
                                .build()
                )
                .build();

        // When
        when(accountOrganizationStorageAdapter.createOrganization(Organization.builder()
                .name(vcsOrganizationName)
                .externalId(externalId)
                .vcsConfiguration(
                        VcsConfiguration.builder()
                                .organizationName(vcsOrganizationName)
                                .build()
                )
                .build())).thenReturn(
                expectedOrganization
        );
        final Organization result = organizationService.createOrganizationForVcsNameAndExternalId(vcsOrganizationName
                , externalId);

        // Then
        assertThat(result).isEqualTo(expectedOrganization);
    }
}
