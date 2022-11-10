package io.symeo.monolithic.backend.bootstrap.it.bff;

import io.symeo.monolithic.backend.bff.contract.api.model.CreateApiKeyRequestContract;
import io.symeo.monolithic.backend.domain.bff.model.account.User;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OnboardingEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationApiKeyEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.UserEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationApiKeyRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.BodyInserters;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SymeoApiKeysApiIT extends AbstractSymeoBackForFrontendApiIT {
    @Autowired
    public UserRepository userRepository;

    @Autowired
    public OrganizationRepository organizationRepository;

    @Autowired
    public OrganizationApiKeyRepository organizationApiKeyRepository;

    private static final UUID organizationId = UUID.randomUUID();
    private static final String organizationName = faker.rickAndMorty().character();
    private static final UUID activeUserId = UUID.randomUUID();

    @AfterEach
    public void tearDown() {
        this.organizationApiKeyRepository.deleteAll();
    }

    @Order(1)
    @Test
    void should_create_the_api_key() {
        // Given
        final OrganizationEntity organizationEntity = OrganizationEntity.builder()
                .id(organizationId)
                .name(organizationName)
                .build();
        organizationRepository.save(organizationEntity);

        final String email = faker.gameOfThrones().character();
        UserMapper.entityToDomain(userRepository.save(
                UserEntity.builder()
                        .id(activeUserId)
                        .onboardingEntity(OnboardingEntity.builder().id(UUID.randomUUID()).hasConfiguredTeam(true).hasConnectedToVcs(true).build())
                        .organizationEntities(List.of(organizationEntity))
                        .status(User.ACTIVE)
                        .email(email)
                        .build()
        ));
        authenticationContextProvider.authorizeUserForMail(email);

        final CreateApiKeyRequestContract createApiKeyRequestContract = new CreateApiKeyRequestContract();
        createApiKeyRequestContract.setName(faker.rickAndMorty().character());

        // When
        client.post()
            .uri(getApiURI(API_KEYS_REST_API_TESTING))
            .body(BodyInserters.fromValue(createApiKeyRequestContract))
            .exchange()
            // Then
            .expectStatus()
            .is2xxSuccessful()
            .expectBody()
            .jsonPath("$.errors").isEmpty();

        assertThat(organizationApiKeyRepository.findAll().size()).isEqualTo(1);
    }

    @Order(2)
    @Test
    void should_fetch_the_current_user_api_keys() {
        // Given
        final OrganizationEntity organizationEntity = OrganizationEntity.builder()
                .id(organizationId)
                .name(organizationName)
                .build();
        organizationRepository.save(organizationEntity);

        final String email = faker.gameOfThrones().character();
        UserMapper.entityToDomain(userRepository.save(
                UserEntity.builder()
                        .id(activeUserId)
                        .onboardingEntity(OnboardingEntity.builder().id(UUID.randomUUID()).hasConfiguredTeam(true).hasConnectedToVcs(true).build())
                        .organizationEntities(List.of(organizationEntity))
                        .status(User.ACTIVE)
                        .email(email)
                        .build()
        ));
        authenticationContextProvider.authorizeUserForMail(email);

        OrganizationApiKeyEntity apiKey1 = OrganizationApiKeyEntity.builder()
                .id(UUID.randomUUID())
                .key(UUID.randomUUID().toString())
                .organizationId(organizationId)
                .name(faker.rickAndMorty().character())
                .build();

        OrganizationApiKeyEntity apiKey2 = OrganizationApiKeyEntity.builder()
                .id(UUID.randomUUID())
                .key(UUID.randomUUID().toString())
                .organizationId(organizationId)
                .name(faker.rickAndMorty().character())
                .build();

        OrganizationApiKeyEntity apiKey3 = OrganizationApiKeyEntity.builder()
                .id(UUID.randomUUID())
                .key(UUID.randomUUID().toString())
                .organizationId(UUID.randomUUID())
                .name(faker.rickAndMorty().character())
                .build();

        organizationApiKeyRepository.save(apiKey1);
        organizationApiKeyRepository.save(apiKey2);
        organizationApiKeyRepository.save(apiKey3);

        // When
        client.get()
            .uri(getApiURI(API_KEYS_REST_API_TESTING))
            .exchange()
            // Then
            .expectStatus()
            .is2xxSuccessful()
            .expectBody()
            .jsonPath("$.errors").isEmpty()
            .jsonPath("$.api_keys").isNotEmpty()
            .jsonPath("$.api_keys.length()").isEqualTo(2)
            .jsonPath("$.api_keys[0].id").isEqualTo(apiKey1.getId().toString())
            .jsonPath("$.api_keys[0].value").isEqualTo("************" + apiKey1.getKey().substring(apiKey1.getKey().length() - 5, apiKey1.getKey().length() - 1))
            .jsonPath("$.api_keys[0].name").isEqualTo(apiKey1.getName())
            .jsonPath("$.api_keys[1].id").isEqualTo(apiKey2.getId().toString())
            .jsonPath("$.api_keys[1].value").isEqualTo("************" + apiKey2.getKey().substring(apiKey2.getKey().length() - 5, apiKey2.getKey().length() - 1))
            .jsonPath("$.api_keys[1].name").isEqualTo(apiKey2.getName());
    }

    @Order(3)
    @Test
    void should_delete_user_api_key() {
        // Given
        final OrganizationEntity organizationEntity = OrganizationEntity.builder()
                .id(organizationId)
                .name(organizationName)
                .build();
        organizationRepository.save(organizationEntity);

        final String email = faker.gameOfThrones().character();
        UserMapper.entityToDomain(userRepository.save(
                UserEntity.builder()
                        .id(activeUserId)
                        .onboardingEntity(OnboardingEntity.builder().id(UUID.randomUUID()).hasConfiguredTeam(true).hasConnectedToVcs(true).build())
                        .organizationEntities(List.of(organizationEntity))
                        .status(User.ACTIVE)
                        .email(email)
                        .build()
        ));
        authenticationContextProvider.authorizeUserForMail(email);

        OrganizationApiKeyEntity apiKey = OrganizationApiKeyEntity.builder()
                .id(UUID.randomUUID())
                .key(UUID.randomUUID().toString())
                .organizationId(organizationId)
                .name(faker.rickAndMorty().character())
                .build();

        organizationApiKeyRepository.save(apiKey);

        // When
        client.delete()
            .uri(getApiURI(API_KEYS_REST_API_TESTING, "api_key_id", apiKey.getId().toString()))
            .exchange()
            // Then
            .expectStatus()
            .is2xxSuccessful();

        assertThat(organizationApiKeyRepository.findAll().size()).isEqualTo(0);
    }

    @Order(4)
    @Test
    void should_not_delete_api_key_not_belonging_to_user() {
        // Given
        final OrganizationEntity organizationEntity = OrganizationEntity.builder()
                .id(organizationId)
                .name(organizationName)
                .build();
        organizationRepository.save(organizationEntity);

        final String email = faker.gameOfThrones().character();
        UserMapper.entityToDomain(userRepository.save(
                UserEntity.builder()
                        .id(activeUserId)
                        .onboardingEntity(OnboardingEntity.builder().id(UUID.randomUUID()).hasConfiguredTeam(true).hasConnectedToVcs(true).build())
                        .organizationEntities(List.of(organizationEntity))
                        .status(User.ACTIVE)
                        .email(email)
                        .build()
        ));
        authenticationContextProvider.authorizeUserForMail(email);

        OrganizationApiKeyEntity apiKey = OrganizationApiKeyEntity.builder()
                .id(UUID.randomUUID())
                .key(UUID.randomUUID().toString())
                .organizationId(UUID.randomUUID())
                .name(faker.rickAndMorty().character())
                .build();

        organizationApiKeyRepository.save(apiKey);

        // When
        client.delete()
            .uri(getApiURI(API_KEYS_REST_API_TESTING, "api_key_id", apiKey.getId().toString()))
            .exchange()
            // Then
            .expectStatus()
            .is2xxSuccessful();

        assertThat(organizationApiKeyRepository.findAll().size()).isEqualTo(1);
    }
}
