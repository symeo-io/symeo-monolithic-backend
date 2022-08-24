package io.symeo.monolithic.backend.infrastructure.postgres.adapter;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import io.symeo.monolithic.backend.infrastructure.postgres.PostgresAccountOrganizationAdapter;
import io.symeo.monolithic.backend.infrastructure.postgres.SetupConfiguration;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.VcsOrganizationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = SetupConfiguration.class)
public class PostgresOrganizationAdapterTestIT {

    private final Faker faker = new Faker();
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private VcsOrganizationRepository vcsOrganizationRepository;

    private PostgresAccountOrganizationAdapter postgresOrganizationAdapter;

    @AfterEach
    void tearDown() {
        vcsOrganizationRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @BeforeEach
    void setUp() {
        postgresOrganizationAdapter = new PostgresAccountOrganizationAdapter(vcsOrganizationRepository,
                organizationRepository);
    }

    @Test
    void should_create_an_organization() throws SymeoException {
        // Given
        final String externalId = faker.name().firstName();
        final String name = faker.pokemon().name();
        final Organization organization = Organization.builder()
                .name(name)
                .vcsOrganization(VcsOrganization.builder()
                        .name(faker.name().bloodGroup())
                        .vcsId(faker.dragonBall().character())
                        .externalId(externalId).build())
                .build();

        // When
        final Organization result = postgresOrganizationAdapter.createOrganization(organization);

        // Then
        assertThat(result).isNotNull();
        assertThat(organizationRepository.findAll()).hasSize(1);
    }

    @Test
    void should_find_an_organization_by_vcs_organization_name() throws SymeoException {
        // Given
        final String externalId = faker.name().firstName();
        final String name = faker.pokemon().name();
        final String vcsOrganizationName = faker.name().bloodGroup();
        final UUID organizationId = UUID.randomUUID();
        final Organization organization = Organization.builder()
                .name(name)
                .id(organizationId)
                .vcsOrganization(VcsOrganization.builder()
                        .name(vcsOrganizationName)
                        .vcsId(faker.dragonBall().character())
                        .externalId(externalId).build())
                .build();

        // When
        postgresOrganizationAdapter.createOrganization(organization);
        final Organization result = postgresOrganizationAdapter.findOrganizationById(organizationId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getId()).isEqualTo(organization.getId());
    }

    @Test
    void should_raise_an_exception_for_an_organization_not_existing() {
        // Given
        final UUID organizationId = UUID.randomUUID();

        // When
        SymeoException symeoException = null;
        try {
            postgresOrganizationAdapter.findOrganizationById(organizationId);
        } catch (SymeoException e) {
            symeoException = e;
        }

        // Then
        assertThat(symeoException).isNotNull();
        assertThat(symeoException.getMessage()).isEqualTo(String.format("Organization not found for id %s",
                organizationId));
        assertThat(symeoException.getCode()).isEqualTo("F.ORGANIZATION_NAME_NOT_FOUND");
    }
}
