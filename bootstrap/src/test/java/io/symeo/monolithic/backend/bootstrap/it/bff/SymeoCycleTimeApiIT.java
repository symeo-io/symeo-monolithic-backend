package io.symeo.monolithic.backend.bootstrap.it.bff;

import io.symeo.monolithic.backend.domain.bff.model.account.User;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.DeployDetectionTypeDomainEnum;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.*;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.*;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationSettingsRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.*;
import io.symeo.monolithic.backend.job.domain.model.vcs.CycleTime;
import io.symeo.monolithic.backend.job.domain.model.vcs.PullRequest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDateTime;
import static java.time.ZonedDateTime.ofInstant;

public class SymeoCycleTimeApiIT extends AbstractSymeoBackForFrontendApiIT {

    @Autowired
    public UserRepository userRepository;
    @Autowired
    public OrganizationRepository organizationRepository;
    @Autowired
    public OrganizationSettingsRepository organizationSettingsRepository;
    @Autowired
    public RepositoryRepository repositoryRepository;
    @Autowired
    public TeamRepository teamRepository;
    @Autowired
    public PullRequestRepository pullRequestRepository;
    @Autowired
    public CommitRepository commitRepository;
    @Autowired
    public TagRepository tagRepository;
    @Autowired
    public CycleTimeRepository cycleTimeRepository;

    private static final UUID organizationId = UUID.randomUUID();
    private static final UUID activeUserId = UUID.randomUUID();
    private static final UUID teamId = UUID.randomUUID();
    private static final String repositoryId = faker.gameOfThrones().character();

    @Order(1)
    @Test
    void should_not_compute_cycle_time_metrics_for_no_data_on_time_range() throws SymeoException {
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
        final RepositoryEntity repositoryEntity = repositoryRepository.save(
                RepositoryEntity.builder()
                        .defaultBranch(faker.ancient().hero())
                        .name(faker.name().lastName())
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

        final String startDate = "2022-02-01";
        final String endDate = "2022-04-01";

        // When
        client.get()
                .uri(getApiURI(TEAMS_REST_API_CYCLE_TIME, Map.of("team_id", teamId.toString(),
                        "start_date", startDate, "end_date", endDate)))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.cycle_time.current_start_date").isEqualTo("2022-02-01")
                .jsonPath("$.cycle_time.current_end_date").isEqualTo("2022-04-01")
                .jsonPath("$.cycle_time.previous_start_date").isEqualTo("2021-12-05")
                .jsonPath("$.cycle_time.previous_end_date").isEqualTo("2022-02-01")
                .jsonPath("$.cycle_time.average.value").isEmpty()
                .jsonPath("$.cycle_time.average.tendency_percentage").isEmpty()
                .jsonPath("$.cycle_time.coding_time.value").isEmpty()
                .jsonPath("$.cycle_time.coding_time.tendency_percentage").isEmpty()
                .jsonPath("$.cycle_time.review_time.value").isEmpty()
                .jsonPath("$.cycle_time.review_time.tendency_percentage").isEmpty()
                .jsonPath("$.cycle_time.time_to_deploy.value").isEmpty()
                .jsonPath("$.cycle_time.time_to_deploy.tendency_percentage").isEmpty();
    }

    @Order(2)
    @Test
    void should_compute_cycle_time_metrics_given_previous_and_current_cycle_times() throws SymeoException {
        // Given
        final String startDate = "2022-02-01";
        final String endDate = "2022-04-01";

        final CycleTimeEntity currentCycleTimeEntity1 = CycleTimeEntity.builder()
                .id(faker.dragonBall().character() + "-current-1")
                .value(100L)
                .codingTime(200L)
                .reviewTime(300L)
                .timeToDeploy(400L)
                .deployDate(stringToDate("2022-02-15"))
                .pullRequestId(faker.rickAndMorty().character() + "-current-1")
                .pullRequestAuthorLogin(faker.name().firstName())
                .pullRequestMergeDate(stringToDate("2022-02-13"))
                .pullRequestUpdateDate(stringToDate("2022-02-13"))
                .pullRequestCreationDate(stringToDate("2022-02-10"))
                .pullRequestVcsRepositoryId(repositoryId)
                .pullRequestVcsRepository(faker.howIMetYourMother().character())
                .pullRequestVcsUrl(faker.backToTheFuture().character())
                .pullRequestState("merge")
                .pullRequestTitle(faker.harryPotter().character())
                .pullRequestHead("feature/test-current-1")
                .build();
        final CycleTimeEntity currentCycleTimeEntity2 = CycleTimeEntity.builder()
                .id(faker.dragonBall().character() + "-current-2")
                .value(200L)
                .codingTime(300L)
                .reviewTime(400L)
                .timeToDeploy(500L)
                .deployDate(stringToDate("2022-02-20"))
                .pullRequestId(faker.rickAndMorty().character() + "-current-2")
                .pullRequestAuthorLogin(faker.name().firstName())
                .pullRequestMergeDate(stringToDate("2022-02-17"))
                .pullRequestUpdateDate(stringToDate("2022-02-17"))
                .pullRequestCreationDate(stringToDate("2022-02-09"))
                .pullRequestVcsRepositoryId(repositoryId)
                .pullRequestVcsRepository(faker.howIMetYourMother().character())
                .pullRequestVcsUrl(faker.backToTheFuture().character())
                .pullRequestState("merge")
                .pullRequestTitle(faker.harryPotter().character())
                .pullRequestHead("feature/test-current-2")
                .build();

        final CycleTimeEntity previousCycleTimeEntity1 = CycleTimeEntity.builder()
                .id(faker.dragonBall().character() + "-previous-1")
                .value(50L)
                .codingTime(100L)
                .reviewTime(300L)
                .timeToDeploy(200L)
                .deployDate(stringToDate("2022-01-15"))
                .pullRequestId(faker.rickAndMorty().character() + "-previous-1")
                .pullRequestAuthorLogin(faker.name().firstName())
                .pullRequestMergeDate(stringToDate("2022-01-13"))
                .pullRequestUpdateDate(stringToDate("2022-01-13"))
                .pullRequestCreationDate(stringToDate("2022-01-10"))
                .pullRequestVcsRepositoryId(repositoryId)
                .pullRequestVcsRepository(faker.howIMetYourMother().character())
                .pullRequestVcsUrl(faker.backToTheFuture().character())
                .pullRequestState("merge")
                .pullRequestTitle(faker.harryPotter().character())
                .pullRequestHead("feature/test-previous-1")
                .build();
        final CycleTimeEntity previousCycleTimeEntity2 = CycleTimeEntity.builder()
                .id(faker.dragonBall().character() + "-previous-2")
                .value(150L)
                .codingTime(300L)
                .reviewTime(900L)
                .timeToDeploy(10L)
                .deployDate(stringToDate("2022-01-20"))
                .pullRequestId(faker.rickAndMorty().character() + "-previous-2")
                .pullRequestAuthorLogin(faker.name().firstName())
                .pullRequestMergeDate(stringToDate("2022-01-17"))
                .pullRequestUpdateDate(stringToDate("2022-01-17"))
                .pullRequestCreationDate(stringToDate("2022-01-09"))
                .pullRequestVcsRepositoryId(repositoryId)
                .pullRequestVcsRepository(faker.howIMetYourMother().character())
                .pullRequestVcsUrl(faker.backToTheFuture().character())
                .pullRequestState("merge")
                .pullRequestTitle(faker.harryPotter().character())
                .pullRequestHead("feature/test-previous-2")
                .build();

        cycleTimeRepository.saveAll(List.of(
                currentCycleTimeEntity1, currentCycleTimeEntity2, previousCycleTimeEntity1, previousCycleTimeEntity2
        ));

        // When
        client.get()
                .uri(getApiURI(TEAMS_REST_API_CYCLE_TIME, Map.of("team_id", teamId.toString(),
                        "start_date", startDate, "end_date", endDate)))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.cycle_time.current_start_date").isEqualTo("2022-02-01")
                .jsonPath("$.cycle_time.current_end_date").isEqualTo("2022-04-01")
                .jsonPath("$.cycle_time.previous_start_date").isEqualTo("2021-12-05")
                .jsonPath("$.cycle_time.previous_end_date").isEqualTo("2022-02-01")
                .jsonPath("$.cycle_time.average.value").isEqualTo("150.0")
                .jsonPath("$.cycle_time.average.tendency_percentage").isEqualTo("50.0")
                .jsonPath("$.cycle_time.coding_time.value").isEqualTo("250.0")
                .jsonPath("$.cycle_time.coding_time.tendency_percentage").isEqualTo("25.0")
                .jsonPath("$.cycle_time.review_time.value").isEqualTo("350.0")
                .jsonPath("$.cycle_time.review_time.tendency_percentage").isEqualTo("-41.7")
                .jsonPath("$.cycle_time.time_to_deploy.value").isEqualTo("450.0")
                .jsonPath("$.cycle_time.time_to_deploy.tendency_percentage").isEqualTo("328.6");
    }

}
