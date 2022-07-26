package fr.catlean.monolithic.backend.infrastructure.postgres.adapter;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Onboarding;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import fr.catlean.monolithic.backend.infrastructure.postgres.PostgresAccountOrganizationAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.PostgresAccountUserAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.SetupConfiguration;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.OnboardingEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.UserEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.OnboardingMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.OrganizationMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.OnboardingRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static fr.catlean.monolithic.backend.domain.exception.CatleanExceptionCode.POSTGRES_EXCEPTION;
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
    @Autowired
    private OnboardingRepository onboardingRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        vcsOrganizationRepository.deleteAll();
        organizationRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Test
    void should_create_user() throws CatleanException {
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
        assertThat(userEntity.getId()).isEqualTo(user.getId());
        assertThat(userEntity.getEmail()).isEqualTo(user.getEmail());
        assertThat(user.getOnboarding()).isEqualTo(OnboardingMapper.entityToDomain(userEntity.getOnboardingEntity()));
    }

    @Test
    void should_read_user() throws CatleanException {
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

    @Test
    void should_fin_all_users_by_organization() throws CatleanException {
        final PostgresAccountUserAdapter postgresAccountUserAdapter = new PostgresAccountUserAdapter(userRepository,
                vcsOrganizationRepository);
        final Organization organization1 = Organization.builder()
                .name(faker.name().firstName())
                .id(UUID.randomUUID())
                .build();
        final Organization organization2 = Organization.builder()
                .name(faker.name().firstName())
                .id(UUID.randomUUID())
                .build();
        final OrganizationEntity org1 = organizationRepository.save(OrganizationMapper.domainToEntity(organization1));
        final OrganizationEntity org2 = organizationRepository.save(OrganizationMapper.domainToEntity(organization2));
        final OnboardingEntity onboarding1 =
                OnboardingEntity.builder().id(UUID.randomUUID()).hasConfiguredTeam(false).hasConnectedToVcs(false).build();
        final OnboardingEntity onboarding2 =
                OnboardingEntity.builder().id(UUID.randomUUID()).hasConfiguredTeam(false).hasConnectedToVcs(false).build();
        final OnboardingEntity onboarding3 =
                OnboardingEntity.builder().id(UUID.randomUUID()).hasConfiguredTeam(false).hasConnectedToVcs(false).build();
        final OnboardingEntity onboarding4 =
                OnboardingEntity.builder().id(UUID.randomUUID()).hasConfiguredTeam(false).hasConnectedToVcs(false).build();
        final OnboardingEntity onboarding5 =
                OnboardingEntity.builder().id(UUID.randomUUID()).hasConfiguredTeam(false).hasConnectedToVcs(false).build();
        onboardingRepository.saveAll(List.of(onboarding1, onboarding2, onboarding3, onboarding5, onboarding4));
        userRepository.save(UserEntity.builder().status("PENDING").onboardingEntity(onboarding1).id(UUID.randomUUID()).organizationEntity(org1).email(faker.gameOfThrones().character()).build());
        userRepository.save(UserEntity.builder().status("PENDING").onboardingEntity(onboarding2).id(UUID.randomUUID()).organizationEntity(org1).email(faker.dragonBall().character()).build());
        userRepository.save(UserEntity.builder().status("PENDING").onboardingEntity(onboarding3).id(UUID.randomUUID()).organizationEntity(org2).email(faker.harryPotter().character()).build());
        userRepository.save(UserEntity.builder().status("PENDING").onboardingEntity(onboarding4).id(UUID.randomUUID()).organizationEntity(org2).email(faker.cat().breed()).build());
        userRepository.save(UserEntity.builder().status("PENDING").onboardingEntity(onboarding5).id(UUID.randomUUID()).organizationEntity(org2).email(faker.rickAndMorty().character()).build());

        // When
        final List<User> allByOrganization1 = postgresAccountUserAdapter.findAllByOrganization(organization1);
        final List<User> allByOrganization2 = postgresAccountUserAdapter.findAllByOrganization(organization2);

        // Then
        assertThat(allByOrganization1).hasSize(2);
        assertThat(allByOrganization2).hasSize(3);
    }

    @Test
    void should_save_users() throws CatleanException {
        // Given
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .name(faker.name().firstName())
                .build();
        final OrganizationEntity organizationEntity =
                organizationRepository.save(OrganizationMapper.domainToEntity(organization));
        final PostgresAccountUserAdapter postgresAccountUserAdapter = new PostgresAccountUserAdapter(userRepository,
                vcsOrganizationRepository);
        final List<User> users = List.of(
                User.builder().id(UUID.randomUUID()).organization(organization).email(faker.rickAndMorty().character()).onboarding(Onboarding.builder().id(UUID.randomUUID()).build()).build(),
                User.builder().id(UUID.randomUUID()).organization(organization).email(faker.gameOfThrones().character()).onboarding(Onboarding.builder().id(UUID.randomUUID()).build()).build(),
                User.builder().id(UUID.randomUUID()).organization(organization).email(faker.dragonBall().character()).onboarding(Onboarding.builder().id(UUID.randomUUID()).build()).build()
        );

        // When
        final List<User> usersSaved = postgresAccountUserAdapter.saveUsers(users);

        // Then
        assertThat(usersSaved).hasSize(users.size());
        final List<UserEntity> allUsers = userRepository.findAll();
        assertThat(allUsers).hasSize(users.size());
        allUsers.forEach(userEntity -> assertThat(userEntity.getOrganizationEntity()).isEqualTo(organizationEntity));
    }

    @Test
    void should_save_a_user() throws CatleanException {
        // Given
        final PostgresAccountUserAdapter postgresAccountUserAdapter = new PostgresAccountUserAdapter(userRepository,
                vcsOrganizationRepository);

        // When
        postgresAccountUserAdapter.saveUser(User.builder().id(UUID.randomUUID()).email(faker.rickAndMorty().character()).onboarding(Onboarding.builder().id(UUID.randomUUID()).build()).build());

        // Then
        assertThat(userRepository.findAll()).hasSize(1);
    }

    @Test
    void should_raise_an_exception_for_duplicate_email() throws CatleanException {
        // Given
        final PostgresAccountUserAdapter postgresAccountUserAdapter = new PostgresAccountUserAdapter(userRepository,
                vcsOrganizationRepository);
        final String duplicatedMailStub = "duplicated@mail.fr";
        postgresAccountUserAdapter.createUserWithEmail(duplicatedMailStub);

        // When
        CatleanException catleanException = null;
        try {
            postgresAccountUserAdapter.createUserWithEmail(duplicatedMailStub);
        } catch (CatleanException e) {
            catleanException = e;
        }

        // Then
        assertThat(catleanException).isNotNull();
        assertThat(catleanException.getCode()).isEqualTo(POSTGRES_EXCEPTION);
        assertThat(catleanException.getMessage()).isEqualTo(String.format("Failed to create user for mail %s",
                duplicatedMailStub));
    }


    @Test
    void should_remove_user_from_organization_given_an_user_id() throws CatleanException {
        final PostgresAccountUserAdapter postgresAccountUserAdapter = new PostgresAccountUserAdapter(userRepository,
                vcsOrganizationRepository);
        final Organization organization1 = Organization.builder()
                .name(faker.name().firstName())
                .id(UUID.randomUUID())
                .build();
        final OrganizationEntity org1 = organizationRepository.save(OrganizationMapper.domainToEntity(organization1));
        final OnboardingEntity onboarding1 =
                OnboardingEntity.builder().id(UUID.randomUUID()).hasConfiguredTeam(false).hasConnectedToVcs(false).build();
        onboardingRepository.saveAll(List.of(onboarding1));
        final UserEntity userEntity =
                UserEntity.builder().status("PENDING").onboardingEntity(onboarding1).id(UUID.randomUUID()).organizationEntity(org1).email(faker.gameOfThrones().character()).build();
        userRepository.save(userEntity);

        // When
        postgresAccountUserAdapter.removeOrganizationForUserId(userEntity.getId());

        // Then
        final List<UserEntity> allUsers = userRepository.findAll();
        assertThat(allUsers).hasSize(1);
        assertThat(allUsers.get(0).getOrganizationId()).isNull();
    }
}
