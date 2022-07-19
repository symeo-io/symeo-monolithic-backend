package fr.catlean.monolithic.backend.infrastructure.postgres.it.adapter;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.model.account.VcsConfiguration;
import fr.catlean.monolithic.backend.infrastructure.postgres.PostgresAccountOrganizationAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.PostgresUserAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.UserEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.it.SetupConfiguration;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = SetupConfiguration.class)
public class PostgresUserAdapterTestIT {

    private final Faker faker = new Faker();
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrganizationRepository organizationRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void should_create_user() {
        // Given
        final PostgresUserAdapter postgresUserAdapter = new PostgresUserAdapter(userRepository, organizationRepository);
        final String mail = faker.name().title();

        // When
        final User user = postgresUserAdapter.createUserWithMail(mail);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isNotNull();
        final Optional<UserEntity> optionalUserEntity = userRepository.findByMail(user.getMail());
        assertThat(optionalUserEntity).isPresent();
        final UserEntity userEntity = optionalUserEntity.get();
        assertThat(userEntity.getId()).isEqualTo(user.getId().toString());
        assertThat(userEntity.getMail()).isEqualTo(user.getMail());
    }

    @Test
    void should_read_user() {
        // Given
        final PostgresUserAdapter postgresUserAdapter = new PostgresUserAdapter(userRepository, organizationRepository);
        final String mail = faker.name().title();
        postgresUserAdapter.createUserWithMail(mail);

        // When
        final Optional<User> existingUser = postgresUserAdapter.getUserFromMail(mail);
        final Optional<User> notExistingUser = postgresUserAdapter.getUserFromMail(faker.dragonBall().character());

        // Then
        assertThat(existingUser).isPresent();
        final User user = existingUser.get();
        assertThat(user.getId()).isNotNull();
        assertThat(user.getMail()).isEqualTo(mail);
        assertThat(notExistingUser).isEmpty();

    }

    @Test
    void should_update_user_with_organization() throws CatleanException {
        // Given
        final PostgresUserAdapter postgresUserAdapter = new PostgresUserAdapter(userRepository, organizationRepository);
        final PostgresAccountOrganizationAdapter postgresOrganizationAdapter =
                new PostgresAccountOrganizationAdapter(organizationRepository);
        final String externalId = faker.name().firstName();
        final String name = faker.pokemon().name();
        final Organization organization = Organization.builder()
                .externalId(externalId)
                .name(name)
                .vcsConfiguration(VcsConfiguration.builder().build())
                .build();

        // When
        final User user = postgresUserAdapter.createUserWithMail(faker.dragonBall().character());
        final Organization expectedOrganization = postgresOrganizationAdapter.createOrganization(organization);
        final User updateUserWithOrganization = postgresUserAdapter.updateUserWithOrganization(user,
                externalId);

        // Then
        assertThat(updateUserWithOrganization).isNotNull();
        assertThat(updateUserWithOrganization.getOrganization()).isEqualTo(expectedOrganization);
    }
}
