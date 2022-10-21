package io.symeo.monolithic.backend.bootstrap.it.bff;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.User;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
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
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static java.time.ZonedDateTime.ofInstant;
import static java.time.temporal.ChronoUnit.MINUTES;

public class SymeoDeploymentApiIT extends AbstractSymeoBackForFrontendApiIT {

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
    private static final String repositoryName = faker.name().lastName();
    private static final UUID organizationSettingsId = UUID.randomUUID();
    private static final String deployCommitSha1 = faker.rickAndMorty().character() + "-deploy-1";
    private static final String deployCommitSha2 = faker.rickAndMorty().character() + "-deploy-2";
    private static final String deployCommitSha3 = faker.rickAndMorty().character() + "-deploy-3";
    private static final String deployCommitSha4 = faker.rickAndMorty().character() + "-deploy-4";
    private static final String deployCommitSha5 = faker.rickAndMorty().character() + "-deploy-5";
    private static final String mergeCommitSha = faker.rickAndMorty().character() + "-merge";
    private static final LocalDateTime now = LocalDateTime.now();
    private static final String organizationName = faker.rickAndMorty().character();
    private static final String fakeDeployLink = faker.gameOfThrones().character();


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
        final String endDate = "2022-01-02";

        // When
        client.get()
                .uri(getApiURI(TEAMS_REST_API_DEPLOYMENT, Map.of("start_date", startDate, "end_date", endDate,
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
    void should_compute_deployment_for_pull_request_merged_on_branch_regex() throws SymeoException {
        // Given
        final CommitEntity commit1 = CommitEntity.builder()
                .sha(faker.gameOfThrones().character() + "-1")
                .message(faker.harryPotter().character())
                .date(ofInstant(stringToDate("2022-03-01").toInstant(), ZoneId.systemDefault()))
                .repositoryId(repositoryId)
                .authorLogin(faker.harryPotter().character())
                .build();
        final CommitEntity mergeCommit1 = CommitEntity.builder()
                .sha(mergeCommitSha)
                .message(faker.harryPotter().character())
                .date(ofInstant(stringToDate("2022-03-02").toInstant(), ZoneId.systemDefault()))
                .repositoryId(repositoryId)
                .parentShaList(List.of(commit1.getSha()))
                .authorLogin(faker.harryPotter().character())
                .build();
        final CommitEntity commit2 = CommitEntity.builder()
                .sha(faker.gameOfThrones().character() + "-2")
                .message(faker.harryPotter().character())
                .date(ofInstant(stringToDate("2022-03-03").toInstant(), ZoneId.systemDefault()))
                .repositoryId(repositoryId)
                .parentShaList(List.of(mergeCommit1.getSha()))
                .authorLogin(faker.harryPotter().character())
                .build();
        final CommitEntity deployCommit1 = CommitEntity.builder()
                .sha(deployCommitSha1)
                .message(faker.harryPotter().character())
                .date(ofInstant(stringToDate("2022-03-04").toInstant(), ZoneId.systemDefault()))
                .repositoryId(repositoryId)
                .parentShaList(List.of(commit2.getSha()))
                .authorLogin(faker.harryPotter().character())
                .build();
        final CommitEntity commit3 = CommitEntity.builder()
                .sha(faker.gameOfThrones().character() + "-3")
                .message(faker.harryPotter().character())
                .date(ofInstant(stringToDate("2022-03-05").toInstant(), ZoneId.systemDefault()))
                .repositoryId(repositoryId)
                .parentShaList(List.of(commit2.getSha()))
                .authorLogin(faker.harryPotter().character())
                .build();
        final CommitEntity deployCommit2 = CommitEntity.builder()
                .sha(deployCommitSha2)
                .message(faker.harryPotter().character())
                .date(ofInstant(stringToDate("2022-02-24").toInstant(), ZoneId.systemDefault()))
                .repositoryId(repositoryId)
                .parentShaList(List.of(commit2.getSha()))
                .authorLogin(faker.harryPotter().character())
                .build();
        final CommitEntity deployCommit3 = CommitEntity.builder()
                .sha(deployCommitSha3)
                .message(faker.harryPotter().character())
                .date(ofInstant(stringToDate("2022-02-27").toInstant(), ZoneId.systemDefault()))
                .repositoryId(repositoryId)
                .parentShaList(List.of(commit2.getSha()))
                .authorLogin(faker.harryPotter().character())
                .build();
        final CommitEntity deployCommit4 = CommitEntity.builder()
                .sha(deployCommitSha4)
                .message(faker.harryPotter().character())
                .date(ofInstant(stringToDate("2022-02-26").toInstant(), ZoneId.systemDefault()))
                .repositoryId(repositoryId)
                .parentShaList(List.of(commit2.getSha()))
                .authorLogin(faker.harryPotter().character())
                .build();
        final CommitEntity deployCommit5 = CommitEntity.builder()
                .sha(deployCommitSha5)
                .message(faker.harryPotter().character())
                .date(ofInstant(stringToDate("2022-03-05").toInstant(), ZoneId.systemDefault()))
                .repositoryId(repositoryId)
                .parentShaList(List.of(commit2.getSha()))
                .authorLogin(faker.harryPotter().character())
                .build();

        commitRepository.saveAll(List.of(commit1, mergeCommit1, commit2, deployCommit1, commit3, deployCommit2, deployCommit3, deployCommit4, deployCommit5));

        pullRequestRepository.saveAll(List.of(
                PullRequestEntity.builder()
                        .id(faker.dragonBall().character() + "-1")
                        .creationDate(ofInstant(stringToDate("2022-03-01").toInstant(),
                                ZoneId.systemDefault()))
                        .lastUpdateDate(mergeCommit1.getDate())
                        .commitShaList(List.of(commit1.getSha()))
                        .mergeDate(mergeCommit1.getDate())
                        .mergeCommitSha(mergeCommit1.getSha())
                        .head("feature/test")
                        .base("staging")
                        .authorLogin(faker.harryPotter().character())
                        .vcsRepositoryId(repositoryId)
                        .vcsRepository(repositoryName)
                        .state(PullRequest.MERGE)
                        .code("1")
                        .build(),
                PullRequestEntity.builder()
                        .id(faker.dragonBall().character() + "-2")
                        .creationDate(ofInstant(stringToDate("2022-03-03").toInstant(),
                                ZoneId.systemDefault()))
                        .lastUpdateDate(deployCommit1.getDate())
                        .commitShaList(List.of(mergeCommit1.getSha(), commit2.getSha()))
                        .mergeDate(deployCommit1.getDate())
                        .mergeCommitSha(deployCommit1.getSha())
                        .authorLogin(faker.harryPotter().character())
                        .vcsRepositoryId(repositoryId)
                        .vcsRepository(repositoryName)
                        .base("main")
                        .head("staging")
                        .code("2")
                        .state(PullRequest.MERGE)
                        .build(),
                PullRequestEntity.builder()
                        .id(faker.dragonBall().character() + "-3")
                        .creationDate(ofInstant(stringToDate("2022-02-22").toInstant(),
                                ZoneId.systemDefault()))
                        .lastUpdateDate(deployCommit2.getDate())
                        .commitShaList(List.of(commit3.getSha()))
                        .mergeDate(deployCommit2.getDate())
                        .mergeCommitSha(deployCommit2.getSha())
                        .authorLogin(faker.harryPotter().character())
                        .vcsRepositoryId(repositoryId)
                        .vcsRepository(repositoryName)
                        .base("main")
                        .head("staging")
                        .code("3")
                        .state(PullRequest.MERGE)
                        .build(),
                PullRequestEntity.builder()
                        .id(faker.dragonBall().character() + "-4")
                        .creationDate(ofInstant(stringToDate("2022-02-20").toInstant(),
                                ZoneId.systemDefault()))
                        .lastUpdateDate(deployCommit3.getDate())
                        .commitShaList(List.of(commit3.getSha()))
                        .mergeDate(deployCommit3.getDate())
                        .mergeCommitSha(deployCommit3.getSha())
                        .authorLogin(faker.harryPotter().character())
                        .vcsRepositoryId(repositoryId)
                        .vcsRepository(repositoryName)
                        .vcsUrl(fakeDeployLink)
                        .base("main")
                        .head("staging")
                        .code("4")
                        .state(PullRequest.MERGE)
                        .build(),
                PullRequestEntity.builder()
                        .id(faker.dragonBall().character() + "-5")
                        .creationDate(ofInstant(stringToDate("2022-02-21").toInstant(),
                                ZoneId.systemDefault()))
                        .lastUpdateDate(deployCommit4.getDate())
                        .commitShaList(List.of(commit3.getSha()))
                        .mergeDate(deployCommit4.getDate())
                        .mergeCommitSha(deployCommit4.getSha())
                        .authorLogin(faker.harryPotter().character())
                        .vcsRepositoryId(repositoryId)
                        .vcsRepository(repositoryName)
                        .vcsUrl(fakeDeployLink)
                        .base("main")
                        .head("staging")
                        .code("5")
                        .state(PullRequest.MERGE)
                        .build(),
                PullRequestEntity.builder()
                        .id(faker.dragonBall().character() + "-6")
                        .creationDate(ofInstant(stringToDate("2022-03-01").toInstant(),
                                ZoneId.systemDefault()))
                        .lastUpdateDate(deployCommit5.getDate())
                        .commitShaList(List.of(commit3.getSha()))
                        .mergeDate(deployCommit5.getDate())
                        .mergeCommitSha(deployCommit5.getSha())
                        .authorLogin(faker.harryPotter().character())
                        .vcsRepositoryId(repositoryId)
                        .vcsRepository(repositoryName)
                        .vcsUrl(fakeDeployLink)
                        .base("main")
                        .head("staging")
                        .code("6")
                        .state(PullRequest.MERGE)
                        .build()
        ));
        final String startDate = "2022-03-01";
        final String endDate = "2022-03-07";

        // When
        client.get()
                .uri(getApiURI(TEAMS_REST_API_DEPLOYMENT, Map.of("team_id", teamId.toString(),
                        "start_date", startDate, "end_date", endDate)))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.deployment.current_start_date").isEqualTo(startDate)
                .jsonPath("$.deployment.current_end_date").isEqualTo(endDate)
                .jsonPath("$.deployment.previous_start_date").isEqualTo("2022-02-23")
                .jsonPath("$.deployment.previous_end_date").isEqualTo(startDate)
                .jsonPath("$.deployment.deploy_count.value").isEqualTo(2)
                .jsonPath("$.deployment.deploy_count.tendency_percentage").isEqualTo(-33.3)
                .jsonPath("$.deployment.deploys_per_day.value").isEqualTo(0.3f)
                .jsonPath("$.deployment.deploys_per_day.tendency_percentage").isEqualTo(-40.0f)
                .jsonPath("$.deployment.average_time_between_deploys.value").isEqualTo(1440.0f)
                .jsonPath("$.deployment.average_time_between_deploys.tendency_percentage").isEqualTo(-33.3f)
                .jsonPath("$.deployment.last_deploy.value").isEqualTo(
                        MINUTES.between(stringToDate("2022-03-05").toInstant(), now.atZone(ZoneId.of("Europe/Paris")).toInstant())
                )
                .jsonPath("$.deployment.last_deploy.label").isEqualTo(repositoryName)
                .jsonPath("$.deployment.last_deploy.link").isEqualTo(fakeDeployLink);
    }

    @Order(3)
    @Test
    void should_compute_deployment_for_tag_to_deploy_regex() throws SymeoException {
        // Given
        final OrganizationSettingsEntity organizationSettingsEntity = OrganizationSettingsEntity.builder()
                .id(organizationSettingsId)
                .organizationId(organizationId)
                .pullRequestMergedOnBranchRegex(null)
                .tagRegex("^deploy$")
                .build();
        organizationSettingsRepository.save(organizationSettingsEntity);
        final TagEntity tagEntity1 = TagEntity.builder()
                .sha(deployCommitSha1)
                .repositoryId(repositoryId)
                .name("deploy")
                .vcsUrl(fakeDeployLink)
                .build();
        final TagEntity tagEntity2 = TagEntity.builder()
                .sha(deployCommitSha2)
                .repositoryId(repositoryId)
                .name("deploy")
                .vcsUrl(fakeDeployLink)
                .build();
        final TagEntity tagEntity3 = TagEntity.builder()
                .sha(deployCommitSha3)
                .repositoryId(repositoryId)
                .name("deploy")
                .vcsUrl(fakeDeployLink)
                .build();
        final TagEntity tagEntity4 = TagEntity.builder()
                .sha(deployCommitSha4)
                .repositoryId(repositoryId)
                .name("deploy")
                .vcsUrl(fakeDeployLink)
                .build();
        final TagEntity tagEntity5 = TagEntity.builder()
                .sha(deployCommitSha5)
                .repositoryId(repositoryId)
                .name("deploy")
                .vcsUrl(fakeDeployLink)
                .build();
        tagRepository.saveAll(List.of(tagEntity1, tagEntity2, tagEntity3, tagEntity4, tagEntity5));

        final String startDate = "2022-03-01";
        final String endDate = "2022-03-07";

        // When
        client.get()
                .uri(getApiURI(TEAMS_REST_API_DEPLOYMENT, Map.of("team_id", teamId.toString(), "start_date", startDate,
                        "end_date", endDate)))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.deployment.current_start_date").isEqualTo(startDate)
                .jsonPath("$.deployment.current_end_date").isEqualTo(endDate)
                .jsonPath("$.deployment.previous_start_date").isEqualTo("2022-02-23")
                .jsonPath("$.deployment.previous_end_date").isEqualTo(startDate)
                .jsonPath("$.deployment.deploy_count.value").isEqualTo(2)
                .jsonPath("$.deployment.deploy_count.tendency_percentage").isEqualTo(-33.3)
                .jsonPath("$.deployment.deploys_per_day.value").isEqualTo(0.3f)
                .jsonPath("$.deployment.deploys_per_day.tendency_percentage").isEqualTo(-40.0f)
                .jsonPath("$.deployment.average_time_between_deploys.value").isEqualTo(1440.0f)
                .jsonPath("$.deployment.average_time_between_deploys.tendency_percentage").isEqualTo(-33.3f)
                .jsonPath("$.deployment.last_deploy.value").isEqualTo(
                        MINUTES.between(stringToDate("2022-03-05").toInstant(), now.atZone(ZoneId.of("Europe/Paris")).toInstant())
                )
                .jsonPath("$.deployment.last_deploy.label").isEqualTo(repositoryName)
                .jsonPath("$.deployment.last_deploy.link").isEqualTo(fakeDeployLink);
    }
}
