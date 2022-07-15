package fr.catlean.monolithic.backend.infrastructure.postgres.it.adapter;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.VcsConfiguration;
import fr.catlean.monolithic.backend.infrastructure.postgres.PostgresOrganizationAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.it.SetupConfiguration;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = SetupConfiguration.class)
public class PostgresOrganizationAdapterTestIT {

    private final Faker faker = new Faker();
    @Autowired
    private OrganizationRepository organizationRepository;

    @AfterEach
    void tearDown() {
        organizationRepository.deleteAll();
    }

    @Test
    void should_create_an_organization() throws CatleanException {
        // Given
        final PostgresOrganizationAdapter postgresOrganizationAdapter =
                new PostgresOrganizationAdapter(organizationRepository);
        final String externalId = faker.name().firstName();
        final String name = faker.pokemon().name();
        final Organization organization = Organization.builder()
                .externalId(externalId)
                .name(name)
                .vcsConfiguration(VcsConfiguration.builder().build())
                .build();

        // When
        final Organization result = postgresOrganizationAdapter.createOrganization(organization);

        // Then
        assertThat(result).isNotNull();
        assertThat(organizationRepository.findAll()).hasSize(1);
    }
}
