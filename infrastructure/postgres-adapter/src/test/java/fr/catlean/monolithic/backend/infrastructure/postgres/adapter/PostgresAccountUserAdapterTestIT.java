package fr.catlean.monolithic.backend.infrastructure.postgres.adapter;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import fr.catlean.monolithic.backend.infrastructure.postgres.PostgresAccountOrganizationAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.PostgresAccountUserAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.SetupConfiguration;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.UserEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.OnboardingMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.VcsOrganizationRepository;
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
public class PostgresAccountUserAdapterTestIT {

    private final Faker faker = new Faker();
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VcsOrganizationRepository vcsOrganizationRepository;
    @Autowired
    private OrganizationRepository organizationRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        vcsOrganizationRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Test
    void should_create_user() {
        // Given
        final PostgresAccountUserAdapter postgresAccountUserAdapter = new PostgresAccountUserAdapter(userRepository,
                vcsOrganizationRepository);
        final String mail = faker.name().title();

        // When
        final User user = postgresAccountUserAdapter.createUserWithEmail(mail);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isNotNull();
        final Optional<UserEntity> optionalUserEntity = userRepository.findByEmail(user.getEmail());
        assertThat(optionalUserEntity).isPresent();
        final UserEntity userEntity = optionalUserEntity.get();
        assertThat(userEntity.getId()).isEqualTo(user.getId().toString());
        assertThat(userEntity.getEmail()).isEqualTo(user.getEmail());
        assertThat(user.getOnboarding()).isEqualTo(OnboardingMapper.entityToDomain(userEntity.getOnboardingEntity()));
    }

    @Test
    void should_read_user() {
        // Given
        final PostgresAccountUserAdapter postgresAccountUserAdapter = new PostgresAccountUserAdapter(userRepository,
                vcsOrganizationRepository);
        final String mail = faker.name().title();
        postgresAccountUserAdapter.createUserWithEmail(mail);

        // When
        final Optional<User> existingUser = postgresAccountUserAdapter.getUserFromEmail(mail);
        final Optional<User> notExistingUser =
                postgresAccountUserAdapter.getUserFromEmail(faker.dragonBall().character());

        // Then
        assertThat(existingUser).isPresent();
        final User user = existingUser.get();
        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo(mail);
        assertThat(notExistingUser).isEmpty();

    }

    @Test
    void should_update_user_with_organization() throws CatleanException {
        // Given
        final PostgresAccountUserAdapter postgresAccountUserAdapter = new PostgresAccountUserAdapter(userRepository,
                vcsOrganizationRepository);
        final PostgresAccountOrganizationAdapter postgresOrganizationAdapter =
                new PostgresAccountOrganizationAdapter(vcsOrganizationRepository);
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
        final User user = postgresAccountUserAdapter.createUserWithEmail(faker.dragonBall().character());
        final Organization expectedOrganization = postgresOrganizationAdapter.createOrganization(organization);
        final User updateUserWithOrganization = postgresAccountUserAdapter.updateUserWithOrganization(user.toBuilder()
                        .onboarding(user.getOnboarding().toBuilder().hasConnectedToVcs(true).build()).build(),
                externalId);

        // Then
        assertThat(updateUserWithOrganization).isNotNull();
        assertThat(updateUserWithOrganization.getOrganization().getId()).isEqualTo(expectedOrganization.getId());
        assertThat(updateUserWithOrganization.getOnboarding().getHasConnectedToVcs()).isTrue();
    }
}
