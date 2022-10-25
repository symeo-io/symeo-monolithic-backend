package io.symeo.monolithic.backend.bootstrap.it.bff;

import io.symeo.monolithic.backend.domain.bff.model.account.User;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.*;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.CommitEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.PullRequestEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.RepositoryEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.TagEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationSettingsRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.CommitRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.RepositoryRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.TagRepository;
import io.symeo.monolithic.backend.job.domain.model.vcs.PullRequest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDateTime;
import static java.time.ZonedDateTime.ofInstant;

public class SymeoCycleTeamApiIT extends AbstractSymeoBackForFrontendApiIT {

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

    private static final UUID organizationId = UUID.randomUUID();
    private static final UUID activeUserId = UUID.randomUUID();
    private static final UUID teamId = UUID.randomUUID();
    private static final String repositoryId = faker.gameOfThrones().character();
    private static final UUID organizationSettingsId = UUID.randomUUID();
    private static final String deployCommitSha = faker.rickAndMorty().character() + "-2";
    private static final String mergeCommitSha = faker.rickAndMorty().character() + "-merge";

    @Order(1)
    @Test
    void should_not_compute_cycle_time_for_no_data_on_time_range() {
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
                .id(organizationSettingsId)
                .organizationId(organizationEntity.getId())
                .pullRequestMergedOnBranchRegex("^main$")
                .build();
        organizationSettingsRepository.save(organizationSettingsEntity);
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
        final String startDate = "2022-01-01";
        final String endDate = "2022-01-02";

        // When
        client.get()
                .uri(getApiURI(TEAMS_REST_API_CYCLE_TIME, Map.of("start_date", startDate, "end_date", endDate,
                        "team_id", teamId.toString())))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty();
    }

    @Order(2)
    @Test
    void should_compute_cycle_time_for_pull_request_merge_on_branch_regex() {
        // Given
        final CommitEntity commit1 = CommitEntity.builder()
                .sha(faker.rickAndMorty().character() + "-1")
                .message(faker.business().creditCardExpiry())
                .date(ofInstant(stringToDateTime("2022-03-01 12:00:00").toInstant(), ZoneId.systemDefault()))
                .repositoryId(repositoryId)
                .authorLogin(faker.harryPotter().character())
                .build();
        final CommitEntity mergeCommit = CommitEntity.builder()
                .sha(mergeCommitSha)
                .message(faker.business().creditCardExpiry())
                .date(ofInstant(stringToDateTime("2022-03-02 10:50:50").toInstant(), ZoneId.systemDefault()))
                .repositoryId(repositoryId)
                .parentShaList(List.of(commit1.getSha()))
                .authorLogin(faker.harryPotter().character())
                .build();
        final CommitEntity commit2 = CommitEntity.builder()
                .sha(faker.rickAndMorty().character() + "-2")
                .message(faker.business().creditCardExpiry())
                .date(ofInstant(stringToDateTime("2022-03-03 13:40:15").toInstant(), ZoneId.systemDefault()))
                .repositoryId(repositoryId)
                .parentShaList(List.of(mergeCommit.getSha()))
                .authorLogin(faker.harryPotter().character())
                .build();
        final CommitEntity deployCommit = CommitEntity.builder()
                .sha(deployCommitSha)
                .message(faker.business().creditCardExpiry())
                .date(ofInstant(stringToDateTime("2022-03-04 22:00:00").toInstant(), ZoneId.systemDefault()))
                .repositoryId(repositoryId)
                .authorLogin(faker.harryPotter().character())
                .parentShaList(List.of(commit2.getSha()))
                .build();

        commitRepository.saveAll(List.of(commit1, mergeCommit, commit2, deployCommit));
        pullRequestRepository.saveAll(List.of(PullRequestEntity.builder()
                        .id(faker.dragonBall().character())
                        .creationDate(ofInstant(stringToDateTime("2022-03-01 08:00:00").toInstant(),
                                ZoneId.systemDefault()))
                        .lastUpdateDate(mergeCommit.getDate())
                        .commitShaList(List.of(commit1.getSha()))
                        .mergeDate(mergeCommit.getDate())
                        .mergeCommitSha(mergeCommit.getSha())
                        .head("feature/test")
                        .base("staging")
                        .authorLogin(faker.harryPotter().character())
                        .vcsRepositoryId(repositoryId)
                        .state(PullRequest.MERGE)
                        .code("1")
                        .build(),
                PullRequestEntity.builder()
                        .id(faker.rickAndMorty().character())
                        .creationDate(ofInstant(stringToDateTime("2022-03-03 14:00:00").toInstant(),
                                ZoneId.systemDefault()))
                        .lastUpdateDate(deployCommit.getDate())
                        .commitShaList(List.of(mergeCommit.getSha(), commit2.getSha()))
                        .mergeDate(deployCommit.getDate())
                        .mergeCommitSha(deployCommit.getSha())
                        .authorLogin(faker.harryPotter().character())
                        .vcsRepositoryId(repositoryId)
                        .base("main")
                        .head("staging")
                        .code("2")
                        .state(PullRequest.MERGE)
                        .build()
        ));
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
                .jsonPath("$.cycle_time.average.value").isEqualTo("4233.5")
                .jsonPath("$.cycle_time.average.tendency_percentage").isEqualTo("0.0")
                .jsonPath("$.cycle_time.coding_time.value").isEqualTo("1609.0")
                .jsonPath("$.cycle_time.coding_time.tendency_percentage").isEqualTo("0.0")
                .jsonPath("$.cycle_time.review_time.value").isEqualTo("1654.5")
                .jsonPath("$.cycle_time.review_time.tendency_percentage").isEqualTo("0.0")
                .jsonPath("$.cycle_time.time_to_deploy.value").isEqualTo("3549.0")
                .jsonPath("$.cycle_time.time_to_deploy.tendency_percentage").isEqualTo("0.0");
    }

    @Order(3)
    @Test
    void should_compute_cycle_time_for_tag_to_deploy_regex() {
        // Given
        final OrganizationSettingsEntity organizationSettingsEntity = OrganizationSettingsEntity.builder()
                .id(organizationSettingsId)
                .organizationId(organizationId)
                .pullRequestMergedOnBranchRegex(null)
                .tagRegex("^infrastructure-.*")
                .build();
        organizationSettingsRepository.save(organizationSettingsEntity);
        tagRepository.save(
                TagEntity.builder()
                        .sha(deployCommitSha)
                        .repositoryId(repositoryId)
                        .name("infrastructure-demo-95-111-47-212")
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
                .jsonPath("$.cycle_time.average.value").isEqualTo("4233.5")
                .jsonPath("$.cycle_time.average.tendency_percentage").isEqualTo("0.0")
                .jsonPath("$.cycle_time.coding_time.value").isEqualTo("1609.0")
                .jsonPath("$.cycle_time.coding_time.tendency_percentage").isEqualTo("0.0")
                .jsonPath("$.cycle_time.review_time.value").isEqualTo("1654.5")
                .jsonPath("$.cycle_time.review_time.tendency_percentage").isEqualTo("0.0")
                .jsonPath("$.cycle_time.time_to_deploy.value").isEqualTo("3549.0")
                .jsonPath("$.cycle_time.time_to_deploy.tendency_percentage").isEqualTo("0.0");
    }


}
