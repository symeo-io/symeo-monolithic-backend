package io.symeo.monolithic.backend.bootstrap.it.bff;

import io.symeo.monolithic.backend.domain.model.account.User;
import io.symeo.monolithic.backend.frontend.contract.api.model.DeliverySettingsContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.DeployDetectionSettingsContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.OrganizationSettingsContract;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OnboardingEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationSettingsEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.UserEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationSettingsRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class SymeoOrganizationSettingsIT extends AbstractSymeoBackForFrontendApiIT {

    @Autowired
    public UserRepository userRepository;
    @Autowired
    public OrganizationRepository organizationRepository;
    @Autowired
    public OrganizationSettingsRepository organizationSettingsRepository;

    private static final UUID organizationId = UUID.randomUUID();
    private static final UUID activeUserId = UUID.randomUUID();

    @Test
    @Order(1)
    void should_get_organization_settings() {
        // Given
        final OrganizationEntity organizationEntity = OrganizationEntity.builder()
                .id(organizationId)
                .name(faker.rickAndMorty().character())
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
        final OrganizationSettingsEntity organizationSettingsEntity = OrganizationSettingsEntity.builder()
                .id(UUID.randomUUID())
                .organizationId(organizationEntity.getId())
                .tagRegex(faker.name().firstName())
                .pullRequestMergedOnBranchRegex(faker.name().lastName())
                .build();
        organizationSettingsRepository.save(organizationSettingsEntity);

        // When
        client.get()
                .uri(getApiURI(ORGANIZATION_REST_API_SETTINGS))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.settings.id").isEqualTo(organizationSettingsEntity.getId().toString())
                .jsonPath("$.settings.delivery.deploy_detection.tag_regex").isEqualTo(organizationSettingsEntity.getTagRegex())
                .jsonPath("$.settings.delivery.deploy_detection.pull_request_merged_on_branch_regex").isEqualTo(organizationSettingsEntity.getPullRequestMergedOnBranchRegex());
    }

    @Test
    @Order(2)
    void should_update_organization_settings() {
        // Given
        final UUID organizationSettingsId = UUID.randomUUID();
        final OrganizationEntity organizationEntity = OrganizationEntity.builder()
                .id(organizationId)
                .name(faker.rickAndMorty().character())
                .build();
        organizationRepository.save(organizationEntity);

        final OrganizationSettingsEntity organizationSettingsEntityToUpdate = OrganizationSettingsEntity.builder()
                .id(organizationSettingsId)
                .organizationId(organizationEntity.getId())
                .tagRegex(faker.name().firstName())
                .pullRequestMergedOnBranchRegex(faker.name().lastName())
                .build();

        organizationSettingsRepository.save(organizationSettingsEntityToUpdate);

        final OrganizationSettingsContract updateOrganizationSettingsContract = new OrganizationSettingsContract();
        final DeliverySettingsContract deliverySettingsContract = new DeliverySettingsContract();
        final DeployDetectionSettingsContract deployDetectionSettingsContract = new DeployDetectionSettingsContract();
        deployDetectionSettingsContract.setTagRegex(faker.gameOfThrones().character());
        deployDetectionSettingsContract.setPullRequestMergedOnBranchRegex(faker.gameOfThrones().dragon());
        deliverySettingsContract.setDeployDetection(deployDetectionSettingsContract);
        updateOrganizationSettingsContract.setDelivery(deliverySettingsContract);
        updateOrganizationSettingsContract.setId(organizationSettingsId);

        // When
        client.patch()
                .uri(getApiURI(ORGANIZATION_REST_API_SETTINGS))
                .body(BodyInserters.fromValue(updateOrganizationSettingsContract))
                .exchange()

                // Then
                .expectStatus()
                .is2xxSuccessful();
        final OrganizationSettingsEntity updatedOrganizationSettings = organizationSettingsRepository.findById(organizationSettingsId).get();
        assertThat(updatedOrganizationSettings.getTagRegex()).isEqualTo(updateOrganizationSettingsContract.getDelivery().getDeployDetection().getTagRegex());
        assertThat(updatedOrganizationSettings.getPullRequestMergedOnBranchRegex()).isEqualTo(updateOrganizationSettingsContract.getDelivery().getDeployDetection().getPullRequestMergedOnBranchRegex());
    }

    @Test
    @Order(3)
    void should_not_update_organization_settings() {
        {
            // Given
            final UUID organizationSettingsId = UUID.randomUUID();
            final UUID invalidOrganizationId = UUID.randomUUID();
            final OrganizationEntity invalidOrganizationEntity = OrganizationEntity.builder()
                    .id(invalidOrganizationId)
                    .name(faker.gameOfThrones().character())
                    .build();
            final OrganizationEntity organizationEntity = OrganizationEntity.builder()
                    .id(organizationId)
                    .name(faker.rickAndMorty().character())
                    .build();
            organizationRepository.save(organizationEntity);
            organizationRepository.save(invalidOrganizationEntity);

            final OrganizationSettingsEntity organizationSettingsEntityToUpdate = OrganizationSettingsEntity.builder()
                    .id(organizationSettingsId)
                    .organizationId(invalidOrganizationId)
                    .tagRegex(faker.name().firstName())
                    .pullRequestMergedOnBranchRegex(faker.name().lastName())
                    .build();

            organizationSettingsRepository.save(organizationSettingsEntityToUpdate);

            final OrganizationSettingsContract unauthorizedUpdateOrganizationSettingsContract = new OrganizationSettingsContract();
            final DeliverySettingsContract deliverySettingsContract = new DeliverySettingsContract();
            final DeployDetectionSettingsContract deployDetectionSettingsContract = new DeployDetectionSettingsContract();
            deployDetectionSettingsContract.setTagRegex(faker.gameOfThrones().character());
            deployDetectionSettingsContract.setPullRequestMergedOnBranchRegex(faker.gameOfThrones().dragon());
            deliverySettingsContract.setDeployDetection(deployDetectionSettingsContract);
            unauthorizedUpdateOrganizationSettingsContract.setDelivery(deliverySettingsContract);
            unauthorizedUpdateOrganizationSettingsContract.setId(organizationSettingsId);

            // When
            client.patch()
                    .uri(getApiURI(ORGANIZATION_REST_API_SETTINGS))
                    .body(BodyInserters.fromValue(unauthorizedUpdateOrganizationSettingsContract))
                    .exchange()

                    // Then
                    .expectStatus()
                    .is4xxClientError();
            final OrganizationSettingsEntity notUpdatedOrganizationSettings = organizationSettingsRepository.findById(organizationSettingsId).get();
            assertThat(notUpdatedOrganizationSettings.getTagRegex()).isEqualTo(organizationSettingsEntityToUpdate.getTagRegex());
            assertThat(notUpdatedOrganizationSettings.getPullRequestMergedOnBranchRegex()).isEqualTo(organizationSettingsEntityToUpdate.getPullRequestMergedOnBranchRegex());
        }
    }

}
