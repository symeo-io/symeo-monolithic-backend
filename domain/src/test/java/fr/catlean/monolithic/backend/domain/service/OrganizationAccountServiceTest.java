package fr.catlean.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.OrganizationAccount;
import fr.catlean.monolithic.backend.domain.model.account.VcsConfiguration;
import fr.catlean.monolithic.backend.domain.port.out.OrganizationAccountAdapter;
import fr.catlean.monolithic.backend.domain.service.OrganizationAccountService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

public class OrganizationAccountServiceTest {

    private final Faker faker = new Faker();

    // TODO : add unit test raising CatleanException
    @Test
    void should_return_organization_given_a_name() throws CatleanException {
        // Given
        final String organizationName = faker.name().firstName();
        final OrganizationAccountAdapter organizationAccountAdapter = Mockito.mock(OrganizationAccountAdapter.class);
        final OrganizationAccountService organizationAccountService =
                new OrganizationAccountService(organizationAccountAdapter);
        final OrganizationAccount expectedOrganizationAccount =
                OrganizationAccount.builder().name(organizationName).vcsConfiguration(VcsConfiguration.builder().build()).build();
        // When
        Mockito.when(organizationAccountAdapter.findOrganizationForName(organizationName))
                .thenReturn(expectedOrganizationAccount);
        final OrganizationAccount organizationAccount =
                organizationAccountService.getOrganizationForName(organizationName);

        // Then
        assertThat(organizationAccount).isNotNull();
        assertThat(organizationAccount.getName()).isEqualTo(organizationName);
    }
}
