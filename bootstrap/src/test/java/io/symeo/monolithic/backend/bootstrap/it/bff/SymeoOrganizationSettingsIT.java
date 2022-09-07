package io.symeo.monolithic.backend.bootstrap.it.bff;

import io.symeo.monolithic.backend.domain.model.account.User;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OnboardingEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationSettingsEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.UserEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationSettingsRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

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
                .jsonPath("$.settings.delivery.deploy_detection.tag_regex").isEqualTo(organizationSettingsEntity.getTagRegex())
                .jsonPath("$.settings.delivery.deploy_detection.pull_request_merged_on_branch_regex").isEqualTo(organizationSettingsEntity.getPullRequestMergedOnBranchRegex());
    }
}
