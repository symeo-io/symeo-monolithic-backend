package fr.catlean.delivery.processor.domain.unit.service;

import com.github.javafaker.Faker;
import fr.catlean.delivery.processor.domain.model.account.OrganisationAccount;
import fr.catlean.delivery.processor.domain.port.out.OrganisationAccountAdapter;
import fr.catlean.delivery.processor.domain.service.OrganisationAccountService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

public class OrganisationAccountServiceTest {

    private final Faker faker = new Faker();

    @Test
    void should_return_organisation_given_a_name() {
        // Given
        final String organisationName = faker.name().firstName();
        final OrganisationAccountAdapter organisationAccountAdapter = Mockito.mock(OrganisationAccountAdapter.class);
        final OrganisationAccountService organisationAccountService = new OrganisationAccountService(organisationAccountAdapter);
        final OrganisationAccount expectedOrganisationAccount = OrganisationAccount.builder().name(organisationName).build();
        // When
        Mockito.when(organisationAccountAdapter.findOrganisationForName(organisationName))
                .thenReturn(expectedOrganisationAccount);
        final OrganisationAccount organisationAccount = organisationAccountService.getOrganisationForName(organisationName);

        // Then
        assertThat(organisationAccount).isNotNull();
        assertThat(organisationAccount.getName()).isEqualTo(organisationName);
    }
}
