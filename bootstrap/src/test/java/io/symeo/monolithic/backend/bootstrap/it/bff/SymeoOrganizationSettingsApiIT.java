package io.symeo.monolithic.backend.bootstrap.it.bff;

import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import io.symeo.monolithic.backend.domain.bff.model.account.User;
import io.symeo.monolithic.backend.bff.contract.api.model.DeliverySettingsContract;
import io.symeo.monolithic.backend.bff.contract.api.model.DeployDetectionSettingsContract;
import io.symeo.monolithic.backend.bff.contract.api.model.OrganizationSettingsContract;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.DeployDetectionSettings;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.DeployDetectionTypeDomainEnum;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OnboardingEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationSettingsEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.UserEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.RepositoryEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationSettingsRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.RepositoryRepository;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.SymeoDataProcessingJobApiProperties;
import io.symeo.monolithic.backend.job.domain.model.vcs.Repository;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static org.assertj.core.api.Assertions.assertThat;

public class SymeoOrganizationSettingsApiIT extends AbstractSymeoBackForFrontendApiIT {

    @Autowired
    public UserRepository userRepository;
    @Autowired
    public OrganizationRepository organizationRepository;
    @Autowired
    public OrganizationSettingsRepository organizationSettingsRepository;
    @Autowired
    public RepositoryRepository repositoryRepository;
    @Autowired
    SymeoDataProcessingJobApiProperties symeoDataProcessingJobApiProperties;

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
                .deployDetectionType(DeployDetectionTypeDomainEnum.PULL_REQUEST.getValue())
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
                .jsonPath("$.settings.delivery.deploy_detection.pull_request_merged_on_branch_regex").isEqualTo(organizationSettingsEntity.getPullRequestMergedOnBranchRegex())
                .jsonPath("$.settings.delivery.deploy_detection.deploy_detection_type").isEqualTo("pull_request");
    }

    @Test
    @Order(2)
    void should_update_organization_settings_and_launch_update_cycle_time_job() {
        // Given
        final UUID organizationSettingsId = UUID.randomUUID();
        final OrganizationEntity organizationEntity = OrganizationEntity.builder()
                .id(organizationId)
                .name(faker.rickAndMorty().character())
                .build();
        organizationRepository.save(organizationEntity);

        final RepositoryEntity repository = RepositoryEntity.builder()
                .id(faker.cat().name())
                .name(faker.dog().name())
                .organizationId(organizationId)
                .vcsOrganizationId(organizationId.toString())
                .vcsOrganizationName(organizationEntity.getName())
                .build();
        repositoryRepository.save(repository);

        final OrganizationSettingsEntity organizationSettingsEntityToUpdate = OrganizationSettingsEntity.builder()
                .id(organizationSettingsId)
                .organizationId(organizationEntity.getId())
                .tagRegex(faker.name().firstName())
                .pullRequestMergedOnBranchRegex(faker.name().lastName())
                .deployDetectionType(DeployDetectionTypeDomainEnum.PULL_REQUEST.getValue())
                .build();

        organizationSettingsRepository.save(organizationSettingsEntityToUpdate);

        final OrganizationSettingsContract updateOrganizationSettingsContract = new OrganizationSettingsContract();
        final DeliverySettingsContract deliverySettingsContract = new DeliverySettingsContract();
        final DeployDetectionSettingsContract deployDetectionSettingsContract = new DeployDetectionSettingsContract();
        deployDetectionSettingsContract.setTagRegex(faker.gameOfThrones().character());
        deployDetectionSettingsContract.setPullRequestMergedOnBranchRegex(faker.gameOfThrones().dragon());
        deployDetectionSettingsContract.setDeployDetectionType(DeployDetectionSettingsContract.DeployDetectionTypeEnum.TAG);
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
        final OrganizationSettingsEntity updatedOrganizationSettings =
                organizationSettingsRepository.findById(organizationSettingsId).get();
        assertThat(updatedOrganizationSettings.getTagRegex()).isEqualTo(updateOrganizationSettingsContract.getDelivery().getDeployDetection().getTagRegex());
        assertThat(updatedOrganizationSettings.getPullRequestMergedOnBranchRegex()).isEqualTo(updateOrganizationSettingsContract.getDelivery().getDeployDetection().getPullRequestMergedOnBranchRegex());
        assertThat(updatedOrganizationSettings.getDeployDetectionType()).isEqualTo(DeployDetectionTypeDomainEnum.TAG.getValue());
        bffWireMockServer.verify(1,
                RequestPatternBuilder.newRequestPattern().withUrl(DATA_PROCESSING_JOB_REST_API_POST_START_JOB_CYCLE_TIME)
                        .withHeader(symeoDataProcessingJobApiProperties.getHeaderKey(),
                                equalTo(symeoDataProcessingJobApiProperties.getApiKey()))
                        .withRequestBody(equalToJson(String.format("{\n" +
                                "  \"organization_id\" : \"%s\",\n" +
                                "  \"repository_ids\" : [ \"%s\" ],\n" +
                                "  \"deploy_detection_type\" : \"%s\",\n" +
                                "  \"pull_request_merged_on_branch_regex\" : \"%s\",\n" +
                                "  \"tag_regex\" : \"%s\",\n" +
                                "  \"exclude_branch_regexes\" : %s\n" +
                                "}", organizationId, repository.getId(), deployDetectionSettingsContract.getDeployDetectionType(),
                                deployDetectionSettingsContract.getPullRequestMergedOnBranchRegex(),
                                deployDetectionSettingsContract.getTagRegex(),
                                deployDetectionSettingsContract.getBranchRegexesToExclude()))
                        ));
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
                    .deployDetectionType(DeployDetectionTypeDomainEnum.PULL_REQUEST.getValue())
                    .build();

            organizationSettingsRepository.save(organizationSettingsEntityToUpdate);

            final OrganizationSettingsContract unauthorizedUpdateOrganizationSettingsContract =
                    new OrganizationSettingsContract();
            final DeliverySettingsContract deliverySettingsContract = new DeliverySettingsContract();
            final DeployDetectionSettingsContract deployDetectionSettingsContract =
                    new DeployDetectionSettingsContract();
            deployDetectionSettingsContract.setTagRegex(faker.gameOfThrones().character());
            deployDetectionSettingsContract.setPullRequestMergedOnBranchRegex(faker.gameOfThrones().dragon());
            deployDetectionSettingsContract.setDeployDetectionType(DeployDetectionSettingsContract.DeployDetectionTypeEnum.TAG);
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
            final OrganizationSettingsEntity notUpdatedOrganizationSettings =
                    organizationSettingsRepository.findById(organizationSettingsId).get();
            assertThat(notUpdatedOrganizationSettings.getTagRegex()).isEqualTo(organizationSettingsEntityToUpdate.getTagRegex());
            assertThat(notUpdatedOrganizationSettings.getPullRequestMergedOnBranchRegex()).isEqualTo(organizationSettingsEntityToUpdate.getPullRequestMergedOnBranchRegex());
            assertThat(notUpdatedOrganizationSettings.getDeployDetectionType()).isEqualTo(DeployDetectionTypeDomainEnum.PULL_REQUEST.getValue());
        }
    }

}
