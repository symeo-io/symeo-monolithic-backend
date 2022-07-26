package fr.catlean.monolithic.backend.bootstrap.it;

import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.frontend.contract.api.model.UserRequestContract;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.OnboardingEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.UserEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CatleanUserInvitationIT extends AbstractCatleanMonolithicBackendIT {

    @Autowired
    public OrganizationRepository organizationRepository;
    @Autowired
    public UserRepository userRepository;


    @Order(1)
    @Test
    void should_find_all_users_for_organization() {
        // Given
        final OrganizationEntity organizationEntity = organizationRepository.save(
                OrganizationEntity.builder()
                        .id(UUID.randomUUID())
                        .name(faker.rickAndMorty().character())
                        .build()
        );
        final UserEntity activeUser = userRepository.save(
                UserEntity.builder()
                        .id(UUID.randomUUID())
                        .onboardingEntity(OnboardingEntity.builder().id(UUID.randomUUID()).hasConfiguredTeam(true).hasConnectedToVcs(true).build())
                        .organizationEntity(organizationEntity)
                        .status(User.ACTIVE)
                        .email(faker.gameOfThrones().character())
                        .build()
        );
        final UserEntity pendingUser = userRepository.save(UserEntity.builder()
                .id(UUID.randomUUID())
                .onboardingEntity(OnboardingEntity.builder().id(UUID.randomUUID()).hasConfiguredTeam(true).hasConnectedToVcs(true).build())
                .organizationEntity(organizationEntity)
                .status(User.ACTIVE)
                .email(faker.rickAndMorty().character())
                .build());
        authenticationContextProvider.authorizeUserForMail(activeUser.getEmail());

        // When
        client.get()
                .uri(getApiURI(ORGANIZATION_REST_API_USERS))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.users[0].id").isEqualTo(activeUser.getId().toString())
                .jsonPath("$.users[0].email").isEqualTo(activeUser.getEmail())
                .jsonPath("$.users[0].status").isEqualTo(activeUser.getStatus())
                .jsonPath("$.users[1].id").isEqualTo(pendingUser.getId().toString())
                .jsonPath("$.users[1].email").isEqualTo(pendingUser.getEmail())
                .jsonPath("$.users[1].status").isEqualTo(pendingUser.getStatus());
    }

    @Order(2)
    @Test
    void should_create_users_and_send_emails() {
        // Given
        final UserRequestContract userRequestContract1 = new UserRequestContract();
        userRequestContract1.email(faker.dragonBall().character());
        final UserRequestContract userRequestContract2 = new UserRequestContract();
        userRequestContract2.email(faker.cat().name());

        // When
        client.post()
                .uri(getApiURI(ORGANIZATION_REST_API_USERS))
                .body(BodyInserters.fromValue(List.of(userRequestContract1, userRequestContract2)))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.users[0].id").isNotEmpty()
                .jsonPath("$.users[0].status").isEqualTo(User.PENDING)
                .jsonPath("$.users[0].email").isEqualTo(userRequestContract1.getEmail())
                .jsonPath("$.users[1].id").isNotEmpty()
                .jsonPath("$.users[1].status").isEqualTo(User.PENDING)
                .jsonPath("$.users[1].email").isEqualTo(userRequestContract2.getEmail());
        assertThat(userRepository.findAll()).hasSize(4);
        final UserEntity userCreated1 = userRepository.findByEmail(userRequestContract1.getEmail()).get();
        final UserEntity userCreated2 = userRepository.findByEmail(userRequestContract2.getEmail()).get();
        assertThat(userCreated1.getOrganizationEntity()).isNotNull();
        assertThat(userCreated2.getOrganizationEntity()).isNotNull();
        assertThat(userCreated1.getOnboardingEntity().getHasConfiguredTeam()).isTrue();
        assertThat(userCreated1.getOnboardingEntity().getHasConnectedToVcs()).isTrue();
        assertThat(userCreated2.getOnboardingEntity().getHasConfiguredTeam()).isTrue();
        assertThat(userCreated2.getOnboardingEntity().getHasConnectedToVcs()).isTrue();
    }

    @Order(3)
    @Test
    void should_active_user_for_first_connection() {
        // Given
        final UserEntity pendingUser =
                userRepository.findAll().stream().filter(userEntity -> userEntity.getStatus().equals(User.PENDING)).findFirst().get();
        authenticationContextProvider.authorizeUserForMail(pendingUser.getEmail());

        // When
        client.get()
                .uri(getApiURI(USER_REST_API_GET_ME))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.user.email").isEqualTo(pendingUser.getEmail())
                .jsonPath("$.user.id").isEqualTo(pendingUser.getId().toString())
                .jsonPath("$.user.onboarding.has_configured_team").isEqualTo(true)
                .jsonPath("$.user.onboarding.has_connected_to_vcs").isEqualTo(true)
                .jsonPath("$.user.organization").isNotEmpty();
        assertThat(userRepository.findByEmail(pendingUser.getEmail()).get().getStatus()).isEqualTo(User.ACTIVE);
    }

    @Order(4)
    @Test
    void should_remove_a_user_from_an_organization_given_an_id() {
        // Given
        final UUID id = userRepository.findAll().get(0).getId();

        // When
        client.delete()
                .uri(getApiURI(ORGANIZATION_REST_API_USERS, "id", id.toString()))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty();
        final Optional<UserEntity> user = userRepository.findById(id);
        assertThat(user.get().getOrganizationEntity()).isNull();
    }
}
