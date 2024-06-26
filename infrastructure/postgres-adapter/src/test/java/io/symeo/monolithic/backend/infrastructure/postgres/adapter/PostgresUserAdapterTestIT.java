package io.symeo.monolithic.backend.infrastructure.postgres.adapter;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.bff.model.account.Onboarding;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.User;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OnboardingEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.UserEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.OnboardingMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.OrganizationMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OnboardingRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationSettingsRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.VcsOrganizationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.POSTGRES_EXCEPTION;
import static org.assertj.core.api.Assertions.assertThat;

public class PostgresUserAdapterTestIT extends AbstractPostgresIT {

    private final Faker faker = new Faker();
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VcsOrganizationRepository vcsOrganizationRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private OnboardingRepository onboardingRepository;
    @Autowired
    private OrganizationSettingsRepository organizationSettingsRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        organizationSettingsRepository.deleteAll();
        vcsOrganizationRepository.deleteAll();
        organizationRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Test
    void should_create_user() throws SymeoException {
        // Given
        final PostgresUserAdapter postgresUserAdapter = new PostgresUserAdapter(userRepository,
                vcsOrganizationRepository);
        final String mail = faker.name().title();

        // When
        final User user = postgresUserAdapter.createUserWithEmail(mail);

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
    void should_read_user() throws SymeoException {
        // Given
        final PostgresUserAdapter postgresUserAdapter = new PostgresUserAdapter(userRepository,
                vcsOrganizationRepository);
        final String mail = faker.name().title();
        postgresUserAdapter.createUserWithEmail(mail);

        // When
        final Optional<User> existingUser = postgresUserAdapter.getUserFromEmail(mail);
        final Optional<User> notExistingUser =
                postgresUserAdapter.getUserFromEmail(faker.dragonBall().character());

        // Then
        assertThat(existingUser).isPresent();
        final User user = existingUser.get();
        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo(mail);
        assertThat(notExistingUser).isEmpty();

    }

    @Test
    void should_update_user_with_organization() throws SymeoException {
        // Given
        final PostgresUserAdapter postgresUserAdapter = new PostgresUserAdapter(userRepository,
                vcsOrganizationRepository);
        final PostgresOrganizationAdapter postgresOrganizationAdapter =
                new PostgresOrganizationAdapter(vcsOrganizationRepository, organizationRepository,
                        organizationSettingsRepository);
        final String externalId = faker.name().firstName();
        final String name = faker.pokemon().name();
        final Organization organization = Organization.builder()
                .name(name)
                .vcsOrganization(Organization.VcsOrganization.builder()
                        .name(faker.name().bloodGroup())
                        .vcsId(faker.dragonBall().character())
                        .externalId(externalId).build())
                .build();

        // When
        final User user = postgresUserAdapter.createUserWithEmail(faker.dragonBall().character());
        final Organization expectedOrganization = postgresOrganizationAdapter.createOrganization(organization);
        final User updateUserWithOrganization = postgresUserAdapter.updateUserWithOrganization(user.toBuilder()
                        .onboarding(user.getOnboarding().toBuilder().hasConnectedToVcs(true).build()).build(),
                externalId);

        // Then
        assertThat(updateUserWithOrganization).isNotNull();
        assertThat(updateUserWithOrganization.getOrganization().getId()).isEqualTo(expectedOrganization.getId());
        assertThat(updateUserWithOrganization.getOnboarding().getHasConnectedToVcs()).isTrue();
    }

    @Test
    void should_fin_all_users_by_organization() throws SymeoException {
        final PostgresUserAdapter postgresUserAdapter = new PostgresUserAdapter(userRepository,
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
        userRepository.save(UserEntity.builder().status("PENDING").onboardingEntity(onboarding1).id(UUID.randomUUID()).organizationEntities(List.of(org1)).email(faker.gameOfThrones().character()).build());
        userRepository.save(UserEntity.builder().status("PENDING").onboardingEntity(onboarding2).id(UUID.randomUUID()).organizationEntities(List.of(org1)).email(faker.dragonBall().character()).build());
        userRepository.save(UserEntity.builder().status("PENDING").onboardingEntity(onboarding3).id(UUID.randomUUID()).organizationEntities(List.of(org2)).email(faker.harryPotter().character()).build());
        userRepository.save(UserEntity.builder().status("PENDING").onboardingEntity(onboarding4).id(UUID.randomUUID()).organizationEntities(List.of(org2)).email(faker.cat().breed()).build());
        userRepository.save(UserEntity.builder().status("PENDING").onboardingEntity(onboarding5).id(UUID.randomUUID()).organizationEntities(List.of(org2)).email(faker.rickAndMorty().character()).build());

        // When
        final List<User> allByOrganization1 = postgresUserAdapter.findAllByOrganization(organization1);
        final List<User> allByOrganization2 = postgresUserAdapter.findAllByOrganization(organization2);

        // Then
        assertThat(allByOrganization1).hasSize(2);
        assertThat(allByOrganization2).hasSize(3);
    }

    @Test
    void should_save_users() throws SymeoException {
        // Given
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .name(faker.name().firstName())
                .build();
        final OrganizationEntity organizationEntity =
                organizationRepository.save(OrganizationMapper.domainToEntity(organization));
        final PostgresUserAdapter postgresUserAdapter = new PostgresUserAdapter(userRepository,
                vcsOrganizationRepository);
        final List<User> users = List.of(
                User.builder().id(UUID.randomUUID()).organizations(List.of(organization)).email(faker.rickAndMorty().character()).onboarding(Onboarding.builder().id(UUID.randomUUID()).build()).build(),
                User.builder().id(UUID.randomUUID()).organizations(List.of(organization)).email(faker.gameOfThrones().character()).onboarding(Onboarding.builder().id(UUID.randomUUID()).build()).build(),
                User.builder().id(UUID.randomUUID()).organizations(List.of(organization)).email(faker.dragonBall().character()).onboarding(Onboarding.builder().id(UUID.randomUUID()).build()).build()
        );

        // When
        final List<User> usersSaved = postgresUserAdapter.saveUsers(users);

        // Then
        assertThat(usersSaved).hasSize(users.size());
        final List<UserEntity> allUsers = userRepository.findAll();
        assertThat(allUsers).hasSize(users.size());
        allUsers.forEach(userEntity -> assertThat(userEntity.getOrganizationEntities().get(0)).isEqualTo(organizationEntity));
    }

    @Test
    void should_save_a_user() throws SymeoException {
        // Given
        final PostgresUserAdapter postgresUserAdapter = new PostgresUserAdapter(userRepository,
                vcsOrganizationRepository);

        // When
        postgresUserAdapter.saveUser(User.builder().id(UUID.randomUUID()).email(faker.rickAndMorty().character()).onboarding(Onboarding.builder().id(UUID.randomUUID()).build()).build());

        // Then
        assertThat(userRepository.findAll()).hasSize(1);
    }

    @Test
    void should_raise_an_exception_for_duplicate_email() throws SymeoException {
        // Given
        final PostgresUserAdapter postgresUserAdapter = new PostgresUserAdapter(userRepository,
                vcsOrganizationRepository);
        final String duplicatedMailStub = "duplicated@mail.fr";
        postgresUserAdapter.createUserWithEmail(duplicatedMailStub);

        // When
        SymeoException symeoException = null;
        try {
            postgresUserAdapter.createUserWithEmail(duplicatedMailStub);
        } catch (SymeoException e) {
            symeoException = e;
        }

        // Then
        assertThat(symeoException).isNotNull();
        assertThat(symeoException.getCode()).isEqualTo(POSTGRES_EXCEPTION);
        assertThat(symeoException.getMessage()).isEqualTo(String.format("Failed to create user for mail %s",
                duplicatedMailStub));
    }


    @Test
    void should_remove_user_from_organization_given_an_user_id() throws SymeoException {
        final PostgresUserAdapter postgresUserAdapter = new PostgresUserAdapter(userRepository,
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
                UserEntity.builder().status("PENDING").onboardingEntity(onboarding1).id(UUID.randomUUID()).organizationEntities(List.of(org1)).email(faker.gameOfThrones().character()).build();
        userRepository.save(userEntity);

        // When
        postgresUserAdapter.removeOrganizationForUserId(userEntity.getId());

        // Then
        final List<UserEntity> allUsers = userRepository.findAll();
        assertThat(allUsers).hasSize(1);
        assertThat(allUsers.get(0).getOrganizationEntities()).hasSize(0);
    }


    @Test
    void should_find_all_users_by_emails() throws SymeoException {
        // Given
        final PostgresUserAdapter postgresUserAdapter = new PostgresUserAdapter(userRepository,
                vcsOrganizationRepository);
        final List<UserEntity> userEntities = userRepository.saveAll(List.of(
                UserEntity.builder().id(UUID.randomUUID()).email(faker.name().name()).status(User.PENDING)
                        .onboardingEntity(OnboardingEntity.builder().id(UUID.randomUUID())
                                .hasConnectedToVcs(false).hasConfiguredTeam(false).build()).build(),
                UserEntity.builder().id(UUID.randomUUID()).email(faker.name().firstName()).status(User.PENDING)
                        .onboardingEntity(OnboardingEntity.builder().id(UUID.randomUUID())
                                .hasConnectedToVcs(false).hasConfiguredTeam(false).build()).build(),
                UserEntity.builder().id(UUID.randomUUID()).email(faker.name().fullName()).status(User.PENDING)
                        .onboardingEntity(OnboardingEntity.builder().id(UUID.randomUUID())
                                .hasConnectedToVcs(false).hasConfiguredTeam(false).build()).build(),
                UserEntity.builder().id(UUID.randomUUID()).email(faker.dragonBall().character()).status(User.PENDING).onboardingEntity(OnboardingEntity.builder().id(UUID.randomUUID())
                                .hasConnectedToVcs(false).hasConfiguredTeam(false).build())
                        .build()
        ));
        final List<String> emails = List.of(userEntities.get(1).getEmail(), userEntities.get(3).getEmail());

        // When
        final List<User> usersFromEmails = postgresUserAdapter.getUsersFromEmails(emails);

        // Then
        assertThat(usersFromEmails).hasSize(emails.size());
        usersFromEmails.stream().map(User::getEmail).forEach(email -> assertThat(emails.contains(email)).isTrue());
    }
}
