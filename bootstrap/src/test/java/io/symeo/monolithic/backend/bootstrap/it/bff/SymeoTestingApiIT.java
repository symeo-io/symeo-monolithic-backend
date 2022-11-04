package io.symeo.monolithic.backend.bootstrap.it.bff;

import io.symeo.monolithic.backend.domain.bff.model.account.User;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.*;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.CommitTestingDataEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.RepositoryEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationSettingsRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.CommitTestingDataRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.RepositoryRepository;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SymeoTestingApiIT extends AbstractSymeoBackForFrontendApiIT {
    @Autowired
    public UserRepository userRepository;
    @Autowired
    public OrganizationRepository organizationRepository;
    @Autowired
    public RepositoryRepository repositoryRepository;
    @Autowired
    public TeamRepository teamRepository;
    @Autowired
    public CommitTestingDataRepository commitTestingDataRepository;
    @Autowired
    public OrganizationSettingsRepository organizationSettingsRepository;

    private static final UUID organizationId = UUID.randomUUID();
    private static final UUID activeUserId = UUID.randomUUID();
    private static final UUID teamId = UUID.randomUUID();
    private static final String repositoryId = faker.gameOfThrones().character();
    private static final String repositoryName = faker.name().lastName();
    private static final UUID organizationSettingsId = UUID.randomUUID();

    private static final String organizationName = faker.rickAndMorty().character();


    @Order(1)
    @Test
    void should_not_compute_deployment_for_no_data_on_time_range() {
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

        final OrganizationSettingsEntity organizationSettingsEntity = OrganizationSettingsEntity.builder()
                .id(organizationSettingsId)
                .organizationId(organizationId)
                .pullRequestMergedOnBranchRegex("^main$")
                .build();
        organizationSettingsRepository.save(organizationSettingsEntity);

        final RepositoryEntity repositoryEntity = repositoryRepository.save(
                RepositoryEntity.builder()
                        .defaultBranch(faker.ancient().hero())
                        .name(repositoryName)
                        .organizationId(organizationId)
                        .id(repositoryId)
                        .build()
        );
        teamRepository.save(
                TeamEntity.builder()
                        .organizationId(organizationId)
                        .id(teamId)
                        .name(faker.gameOfThrones().dragon())
                        .repositoryIds(List.of(repositoryEntity.getId()))
                        .build()
        );

        final String startDate = "2022-01-01";
        final String endDate = "2022-02-01";

        // When
        client.get()
                .uri(getApiURI(TEAMS_REST_API_TESTING, Map.of("start_date", startDate, "end_date", endDate,
                        "team_id", teamId.toString())))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.testing.current_start_date").isEqualTo(startDate)
                .jsonPath("$.testing.current_end_date").isEqualTo(endDate)
                .jsonPath("$.testing.previous_start_date").isEqualTo("2021-12-01")
                .jsonPath("$.testing.previous_end_date").isEqualTo(startDate)
                .jsonPath("$.testing.coverage.value").isEqualTo(null)
                .jsonPath("$.testing.coverage.tendency_percentage").isEqualTo(null)
                .jsonPath("$.testing.test_count.value").isEqualTo(null)
                .jsonPath("$.testing.test_count.tendency_percentage").isEqualTo(null)
                .jsonPath("$.testing.test_to_code_ratio.value").isEqualTo(null)
                .jsonPath("$.testing.test_to_code_ratio.tendency_percentage").isEqualTo(null)
                .jsonPath("$.testing.test_to_code_ratio.code_line_count").isEqualTo(null)
                .jsonPath("$.testing.test_to_code_ratio.test_line_count").isEqualTo(null)
                .jsonPath("$.testing.test_types.unit").isEqualTo(null)
                .jsonPath("$.testing.test_types.unit_tendency_percentage").isEqualTo(null)
                .jsonPath("$.testing.test_types.integration").isEqualTo(null)
                .jsonPath("$.testing.test_types.integration_tendency_percentage").isEqualTo(null)
                .jsonPath("$.testing.test_types.end_to_end").isEqualTo(null)
                .jsonPath("$.testing.test_types.end_to_end_tendency_percentage").isEqualTo(null);
    }

    @Order(2)
    @Test
    void should_compute_testing_metrics_without_history_data() throws SymeoException {
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

        final OrganizationSettingsEntity organizationSettingsEntity = OrganizationSettingsEntity.builder()
                .id(organizationSettingsId)
                .organizationId(organizationId)
                .pullRequestMergedOnBranchRegex("^main$")
                .build();
        organizationSettingsRepository.save(organizationSettingsEntity);

        final RepositoryEntity repositoryEntity = repositoryRepository.save(
                RepositoryEntity.builder()
                        .defaultBranch(faker.ancient().hero())
                        .name(repositoryName)
                        .organizationId(organizationId)
                        .id(repositoryId)
                        .build()
        );
        teamRepository.save(
                TeamEntity.builder()
                        .organizationId(organizationId)
                        .id(teamId)
                        .name(faker.gameOfThrones().dragon())
                        .repositoryIds(List.of(repositoryEntity.getId()))
                        .build()
        );

        commitTestingDataRepository.save(
                CommitTestingDataEntity.builder()
                        .id(UUID.randomUUID())
                        .commitSha(faker.gameOfThrones().dragon())
                        .organizationId(organizationId)
                        .repositoryName(repositoryEntity.getName())
                        .branchName(repositoryEntity.getDefaultBranch())
                        .unitTestCount(100)
                        .integrationTestCount(20)
                        .testLineCount(1000)
                        .codeLineCount(3000)
                        .coveredBranches(100)
                        .totalBranchCount(200)
                        .date(ZonedDateTime.of(
                                2022,
                                1,
                                7,
                                0,
                                0,
                                0,
                                0,
                                ZoneId.systemDefault()))
                        .build()
        );

        commitTestingDataRepository.save(
                CommitTestingDataEntity.builder()
                        .id(UUID.randomUUID())
                        .commitSha(faker.gameOfThrones().dragon())
                        .organizationId(organizationId)
                        .repositoryName(repositoryEntity.getName())
                        .branchName(repositoryEntity.getDefaultBranch())
                        .unitTestCount(150)
                        .integrationTestCount(30)
                        .testLineCount(2000)
                        .codeLineCount(6000)
                        .coveredBranches(150)
                        .totalBranchCount(200)
                        .date(ZonedDateTime.of(
                                2022,
                                1,
                                15,
                                0,
                                0,
                                0,
                                0,
                                ZoneId.systemDefault()))
                        .build()
        );

        // Given
        final String startDate = "2022-01-01";
        final String endDate = "2022-02-01";

        // When
        client.get()
                .uri(getApiURI(TEAMS_REST_API_TESTING, Map.of("team_id", teamId.toString(),
                        "start_date", startDate, "end_date", endDate)))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.testing.current_start_date").isEqualTo(startDate)
                .jsonPath("$.testing.current_end_date").isEqualTo(endDate)
                .jsonPath("$.testing.previous_start_date").isEqualTo("2021-12-01")
                .jsonPath("$.testing.previous_end_date").isEqualTo(startDate)
                .jsonPath("$.testing.coverage.value").isEqualTo(75f)
                .jsonPath("$.testing.coverage.tendency_percentage").isEqualTo(null)
                .jsonPath("$.testing.test_count.value").isEqualTo(180)
                .jsonPath("$.testing.test_count.tendency_percentage").isEqualTo(null)
                .jsonPath("$.testing.test_to_code_ratio.value").isEqualTo(0.25f)
                .jsonPath("$.testing.test_to_code_ratio.tendency_percentage").isEqualTo(null)
                .jsonPath("$.testing.test_to_code_ratio.code_line_count").isEqualTo(6000)
                .jsonPath("$.testing.test_to_code_ratio.test_line_count").isEqualTo(2000)
                .jsonPath("$.testing.test_types.unit").isEqualTo(150)
                .jsonPath("$.testing.test_types.unit_tendency_percentage").isEqualTo(null)
                .jsonPath("$.testing.test_types.integration").isEqualTo(30)
                .jsonPath("$.testing.test_types.integration_tendency_percentage").isEqualTo(null)
                .jsonPath("$.testing.test_types.end_to_end").isEqualTo(null)
                .jsonPath("$.testing.test_types.end_to_end_tendency_percentage").isEqualTo(null);
    }

    @Order(3)
    @Test
    void should_compute_testing_metrics_with_history_data() throws SymeoException {
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

        final OrganizationSettingsEntity organizationSettingsEntity = OrganizationSettingsEntity.builder()
                .id(organizationSettingsId)
                .organizationId(organizationId)
                .pullRequestMergedOnBranchRegex("^main$")
                .build();
        organizationSettingsRepository.save(organizationSettingsEntity);

        final RepositoryEntity repositoryEntity = repositoryRepository.save(
                RepositoryEntity.builder()
                        .defaultBranch(faker.ancient().hero())
                        .name(repositoryName)
                        .organizationId(organizationId)
                        .id(repositoryId)
                        .build()
        );
        teamRepository.save(
                TeamEntity.builder()
                        .organizationId(organizationId)
                        .id(teamId)
                        .name(faker.gameOfThrones().dragon())
                        .repositoryIds(List.of(repositoryEntity.getId()))
                        .build()
        );

        commitTestingDataRepository.save(
                CommitTestingDataEntity.builder()
                        .id(UUID.randomUUID())
                        .commitSha(faker.gameOfThrones().dragon())
                        .organizationId(organizationId)
                        .repositoryName(repositoryEntity.getName())
                        .branchName(repositoryEntity.getDefaultBranch())
                        .unitTestCount(100)
                        .integrationTestCount(20)
                        .testLineCount(1000)
                        .codeLineCount(3000)
                        .coveredBranches(100)
                        .totalBranchCount(200)
                        .date(ZonedDateTime.of(
                                2021,
                                12,
                                15,
                                0,
                                0,
                                0,
                                0,
                                ZoneId.systemDefault()))
                        .build()
        );

        commitTestingDataRepository.save(
                CommitTestingDataEntity.builder()
                        .id(UUID.randomUUID())
                        .commitSha(faker.gameOfThrones().dragon())
                        .organizationId(organizationId)
                        .repositoryName(repositoryEntity.getName())
                        .branchName(repositoryEntity.getDefaultBranch())
                        .unitTestCount(100)
                        .integrationTestCount(20)
                        .testLineCount(1000)
                        .codeLineCount(3000)
                        .coveredBranches(100)
                        .totalBranchCount(200)
                        .date(ZonedDateTime.of(
                                2022,
                                1,
                                7,
                                0,
                                0,
                                0,
                                0,
                                ZoneId.systemDefault()))
                        .build()
        );

        commitTestingDataRepository.save(
                CommitTestingDataEntity.builder()
                        .id(UUID.randomUUID())
                        .commitSha(faker.gameOfThrones().dragon())
                        .organizationId(organizationId)
                        .repositoryName(repositoryEntity.getName())
                        .branchName(repositoryEntity.getDefaultBranch())
                        .unitTestCount(150)
                        .integrationTestCount(30)
                        .testLineCount(2000)
                        .codeLineCount(6000)
                        .coveredBranches(150)
                        .totalBranchCount(200)
                        .date(ZonedDateTime.of(
                                2022,
                                1,
                                15,
                                0,
                                0,
                                0,
                                0,
                                ZoneId.systemDefault()))
                        .build()
        );

        // Given
        final String startDate = "2022-01-01";
        final String endDate = "2022-02-01";

        // When
        client.get()
                .uri(getApiURI(TEAMS_REST_API_TESTING, Map.of("team_id", teamId.toString(),
                        "start_date", startDate, "end_date", endDate)))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.testing.current_start_date").isEqualTo(startDate)
                .jsonPath("$.testing.current_end_date").isEqualTo(endDate)
                .jsonPath("$.testing.previous_start_date").isEqualTo("2021-12-01")
                .jsonPath("$.testing.previous_end_date").isEqualTo(startDate)
                .jsonPath("$.testing.coverage.value").isEqualTo(75f)
                .jsonPath("$.testing.coverage.tendency_percentage").isEqualTo(50f)
                .jsonPath("$.testing.test_count.value").isEqualTo(180)
                .jsonPath("$.testing.test_count.tendency_percentage").isEqualTo(50f)
                .jsonPath("$.testing.test_to_code_ratio.value").isEqualTo(0.25f)
                .jsonPath("$.testing.test_to_code_ratio.tendency_percentage").isEqualTo(0f)
                .jsonPath("$.testing.test_to_code_ratio.code_line_count").isEqualTo(6000)
                .jsonPath("$.testing.test_to_code_ratio.test_line_count").isEqualTo(2000)
                .jsonPath("$.testing.test_types.unit").isEqualTo(150)
                .jsonPath("$.testing.test_types.unit_tendency_percentage").isEqualTo(50f)
                .jsonPath("$.testing.test_types.integration").isEqualTo(30)
                .jsonPath("$.testing.test_types.integration_tendency_percentage").isEqualTo(50f)
                .jsonPath("$.testing.test_types.end_to_end").isEqualTo(null)
                .jsonPath("$.testing.test_types.end_to_end_tendency_percentage").isEqualTo(null);
    }
}
