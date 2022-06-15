package fr.catlean.delivery.processor.domain.unit.service;

import com.github.javafaker.Faker;
import fr.catlean.delivery.processor.domain.model.account.OrganizationAccount;
import fr.catlean.delivery.processor.domain.model.account.VcsConfiguration;
import fr.catlean.delivery.processor.domain.port.out.OrganizationAccountAdapter;
import fr.catlean.delivery.processor.domain.service.OrganizationAccountService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

public class OrganizationAccountServiceTest {

    private final Faker faker = new Faker();

    @Test
    void should_return_organization_given_a_name() {
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
