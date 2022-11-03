package io.symeo.monolithic.backend.bootstrap.it.bff;

import io.symeo.monolithic.backend.domain.bff.model.account.User;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.DeployDetectionTypeDomainEnum;
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
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.dateTimeToString;
import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDateTime;
import static java.time.ZonedDateTime.ofInstant;

public class SymeoCycleTimePieceApiIT extends AbstractSymeoBackForFrontendApiIT {

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
    private static final UUID organizationSettingsId = UUID.randomUUID();
    private static final String repositoryId = faker.gameOfThrones().character();
    private static final String repositoryName = faker.name().lastName();
    private static final UUID teamId = UUID.randomUUID();
    private static final String deployCommitSha = faker.rickAndMorty().character() + "-2";
    private static final String mergeCommitSha = faker.rickAndMorty().character() + "-merge";
    private static final Integer pageIndex = 0;
    private static final Integer pageSize = 5;
    private static final String sortBy = "creation_date";
    private static final String sortDir = "asc";
    private static final String pullRequestId1 = faker.animal().name() + "-1";
    private static final String pullRequestId2 = faker.animal().name() + "-2";
    private static final String pullRequestTitle1 = faker.gameOfThrones().dragon() + "-1";
    private static final String pullRequestTitle2 = faker.gameOfThrones().dragon() + "-2";
    private static final String pullRequestVcsUrl1 = faker.pokemon().name() + "-1";
    private static final String pullRequestVcsUrl2 = faker.pokemon().name() + "-2";
    private static final String pullRequestAuthor1 = faker.harryPotter().character() + "-1";
    private static final String pullRequestAuthor2 = faker.harryPotter().character() + "-2";
    private static final ZonedDateTime pullRequestCreationDate1 = ofInstant(stringToDateTime("2022-03-01 08:00:00").toInstant(),
            ZoneId.systemDefault());
    private static final ZonedDateTime mergeCommitCreationDate = ofInstant(stringToDateTime("2022-03-02 10:50:50").toInstant(),
            ZoneId.systemDefault());
    private static final ZonedDateTime pullRequestCreationDate2 = ofInstant(stringToDateTime("2022-03-03 14:00:00").toInstant(),
            ZoneId.systemDefault());
    private static final ZonedDateTime deployCommitCreationDate = ofInstant(stringToDateTime("2022-03-04 22:00:00").toInstant(),
            ZoneId.systemDefault());


    @Order(1)
    @Test
    void should_not_compute_cycle_time_pieces_for_no_data_on_time_range() {
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
                .deployDetectionType(DeployDetectionTypeDomainEnum.PULL_REQUEST.toString())
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
                .uri(getApiURI(TEAMS_REST_API_CYCLE_TIME_PIECES, Map.of("team_id", teamId.toString(),
                        "start_date", startDate, "end_date", endDate, "page_index", pageIndex.toString(),
                        "page_size", pageSize.toString(), "sort_by", sortBy, "sort_dir", sortDir)))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.pieces_page.pieces").isEmpty()
                .jsonPath("$.pieces_page.total_page_number").isEqualTo(0)
                .jsonPath("$.pieces_page.total_item_number").isEqualTo(0);
    }

    @Order(2)
    @Test
    void should_compute_cycle_time_pieces_for_pull_request_merge_on_branch_regex() {
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
                .date(mergeCommitCreationDate)
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
                .date(deployCommitCreationDate)
                .repositoryId(repositoryId)
                .authorLogin(faker.harryPotter().character())
                .parentShaList(List.of(commit2.getSha()))
                .build();

        commitRepository.saveAll(List.of(commit1, mergeCommit, commit2, deployCommit));
        final List<PullRequestEntity> pullRequestEntities = List.of(
                PullRequestEntity.builder()
                        .id(pullRequestId1)
                        .creationDate(pullRequestCreationDate1)
                        .lastUpdateDate(mergeCommit.getDate())
                        .commitShaList(List.of(commit1.getSha()))
                        .mergeDate(mergeCommit.getDate())
                        .mergeCommitSha(mergeCommit.getSha())
                        .head("feature/test")
                        .base("staging")
                        .authorLogin(pullRequestAuthor1)
                        .vcsRepositoryId(repositoryId)
                        .vcsRepository(repositoryName)
                        .state(PullRequest.MERGE)
                        .code("1")
                        .title(pullRequestTitle1)
                        .vcsUrl(pullRequestVcsUrl1)
                        .build(),
                PullRequestEntity.builder()
                        .id(pullRequestId2)
                        .creationDate(pullRequestCreationDate2)
                        .lastUpdateDate(deployCommit.getDate())
                        .commitShaList(List.of(mergeCommit.getSha(), commit2.getSha()))
                        .mergeDate(deployCommit.getDate())
                        .mergeCommitSha(deployCommit.getSha())
                        .authorLogin(pullRequestAuthor2)
                        .vcsRepositoryId(repositoryId)
                        .vcsRepository(repositoryName)
                        .base("main")
                        .head("staging")
                        .code("2")
                        .title(pullRequestTitle2)
                        .state(PullRequest.MERGE)
                        .vcsUrl(pullRequestVcsUrl2)
                        .build()
        );
        pullRequestRepository.saveAll(pullRequestEntities);

        final String startDate = "2022-02-01";
        final String endDate = "2022-04-01";

        // When
        client.get()
                .uri(getApiURI(TEAMS_REST_API_CYCLE_TIME_PIECES, Map.of("team_id", teamId.toString(),
                        "start_date", startDate, "end_date", endDate, "page_index", pageIndex.toString(),
                        "page_size", pageSize.toString(), "sort_by", sortBy, "sort_dir", sortDir)))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.pieces_page.total_item_number").isEqualTo(2)
                .jsonPath("$.pieces_page.total_page_number").isEqualTo(Math.ceil(1.0f * 2 / pageSize))
                .jsonPath("$.pieces_page.pieces[0].id").isEqualTo(pullRequestId1)
                .jsonPath("$.pieces_page.pieces[0].status").isEqualTo("merge")
                .jsonPath("$.pieces_page.pieces[0].title").isEqualTo(pullRequestTitle1)
                .jsonPath("$.pieces_page.pieces[0].vcs_url").isEqualTo(pullRequestVcsUrl1)
                .jsonPath("$.pieces_page.pieces[0].author").isEqualTo(pullRequestAuthor1)
                .jsonPath("$.pieces_page.pieces[0].vcs_repository").isEqualTo(repositoryName)
                .jsonPath("$.pieces_page.pieces[0].creation_date").isEqualTo(dateTimeToString(Date.from(pullRequestCreationDate1.toInstant())))
                .jsonPath("$.pieces_page.pieces[0].merge_date").isEqualTo(dateTimeToString(Date.from(mergeCommitCreationDate.toInstant())))
                .jsonPath("$.pieces_page.pieces[0].coding_time").isEqualTo(null)
                .jsonPath("$.pieces_page.pieces[0].review_time").isEqualTo("1370")
                .jsonPath("$.pieces_page.pieces[0].time_to_deploy").isEqualTo("3549")
                .jsonPath("$.pieces_page.pieces[0].cycle_time").isEqualTo("4919")
                .jsonPath("$.pieces_page.pieces[1].id").isEqualTo(pullRequestId2)
                .jsonPath("$.pieces_page.pieces[1].status").isEqualTo("merge")
                .jsonPath("$.pieces_page.pieces[1].title").isEqualTo(pullRequestTitle2)
                .jsonPath("$.pieces_page.pieces[1].vcs_url").isEqualTo(pullRequestVcsUrl2)
                .jsonPath("$.pieces_page.pieces[1].author").isEqualTo(pullRequestAuthor2)
                .jsonPath("$.pieces_page.pieces[1].vcs_repository").isEqualTo(repositoryName)
                .jsonPath("$.pieces_page.pieces[1].creation_date").isEqualTo(dateTimeToString(Date.from(pullRequestCreationDate2.toInstant())))
                .jsonPath("$.pieces_page.pieces[1].merge_date").isEqualTo(dateTimeToString(Date.from(deployCommitCreationDate.toInstant())))
                .jsonPath("$.pieces_page.pieces[1].coding_time").isEqualTo("1609")
                .jsonPath("$.pieces_page.pieces[1].review_time").isEqualTo("1939")
                .jsonPath("$.pieces_page.pieces[1].time_to_deploy").isEqualTo(null)
                .jsonPath("$.pieces_page.pieces[1].cycle_time").isEqualTo("3548");
    }

    @Order(3)
    @Test
    void should_get_cycle_time_piece_curve_for_pull_request_merge_on_branch_regex() {
        // Given
        final String startDate = "2022-02-01";
        final String endDate = "2022-04-01";

        // When
        client.get()
                .uri(getApiURI(TEAMS_REST_API_CYCLE_TIME_CURVE, Map.of("team_id", teamId.toString(),
                        "start_date", startDate, "end_date", endDate)))
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.curves.piece_curve[0]").exists()
                .jsonPath("$.curves.average_curve[0].value").isEqualTo(4919.0f)
                .jsonPath("$.curves.average_curve[0].date").isEqualTo("2022-03-04");

    }


    @Order(4)
    @Test
    void should_compute_cycle_time_pieces_for_tag_to_deploy_regex() {
        // Given
        final OrganizationSettingsEntity organizationSettingsEntity = OrganizationSettingsEntity.builder()
                .id(organizationSettingsId)
                .organizationId(organizationId)
                .pullRequestMergedOnBranchRegex(null)
                .tagRegex("^infrastructure-.*")
                .deployDetectionType(DeployDetectionTypeDomainEnum.TAG.toString())
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
                .uri(getApiURI(TEAMS_REST_API_CYCLE_TIME_PIECES, Map.of("team_id", teamId.toString(),
                        "start_date", startDate, "end_date", endDate, "page_index", pageIndex.toString(),
                        "page_size", pageSize.toString(), "sort_by", sortBy, "sort_dir", sortDir)))
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.pieces_page.total_item_number").isEqualTo(2)
                .jsonPath("$.pieces_page.total_page_number").isEqualTo(Math.ceil(1.0f * 2 / pageSize))
                .jsonPath("$.pieces_page.pieces[0].id").isEqualTo(pullRequestId1)
                .jsonPath("$.pieces_page.pieces[0].status").isEqualTo("merge")
                .jsonPath("$.pieces_page.pieces[0].title").isEqualTo(pullRequestTitle1)
                .jsonPath("$.pieces_page.pieces[0].vcs_url").isEqualTo(pullRequestVcsUrl1)
                .jsonPath("$.pieces_page.pieces[0].author").isEqualTo(pullRequestAuthor1)
                .jsonPath("$.pieces_page.pieces[0].vcs_repository").isEqualTo(repositoryName)
                .jsonPath("$.pieces_page.pieces[0].creation_date").isEqualTo(dateTimeToString(Date.from(pullRequestCreationDate1.toInstant())))
                .jsonPath("$.pieces_page.pieces[0].merge_date").isEqualTo(dateTimeToString(Date.from(mergeCommitCreationDate.toInstant())))
                .jsonPath("$.pieces_page.pieces[0].coding_time").isEqualTo(null)
                .jsonPath("$.pieces_page.pieces[0].review_time").isEqualTo("1370")
                .jsonPath("$.pieces_page.pieces[0].time_to_deploy").isEqualTo("3549")
                .jsonPath("$.pieces_page.pieces[0].cycle_time").isEqualTo("4919")

                .jsonPath("$.pieces_page.pieces[1].id").isEqualTo(pullRequestId2)
                .jsonPath("$.pieces_page.pieces[1].status").isEqualTo("merge")
                .jsonPath("$.pieces_page.pieces[1].title").isEqualTo(pullRequestTitle2)
                .jsonPath("$.pieces_page.pieces[1].vcs_url").isEqualTo(pullRequestVcsUrl2)
                .jsonPath("$.pieces_page.pieces[1].author").isEqualTo(pullRequestAuthor2)
                .jsonPath("$.pieces_page.pieces[1].vcs_repository").isEqualTo(repositoryName)
                .jsonPath("$.pieces_page.pieces[1].creation_date").isEqualTo(dateTimeToString(Date.from(pullRequestCreationDate2.toInstant())))
                .jsonPath("$.pieces_page.pieces[1].merge_date").isEqualTo(dateTimeToString(Date.from(deployCommitCreationDate.toInstant())))
                .jsonPath("$.pieces_page.pieces[1].coding_time").isEqualTo("1609")
                .jsonPath("$.pieces_page.pieces[1].review_time").isEqualTo("1939")
                .jsonPath("$.pieces_page.pieces[1].time_to_deploy").isEqualTo(null)
                .jsonPath("$.pieces_page.pieces[1].cycle_time").isEqualTo("3548");
    }

    @Order(5)
    @Test
    void should_get_cycle_time_piece_curve_for_tag_to_deploy_regex() {
        // Given
        final String startDate = "2022-02-01";
        final String endDate = "2022-04-01";

        // When
        client.get()
                .uri(getApiURI(TEAMS_REST_API_CYCLE_TIME_CURVE, Map.of("team_id", teamId.toString(),
                        "start_date", startDate, "end_date", endDate)))
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.curves.piece_curve[0]").exists()
                .jsonPath("$.curves.average_curve[0].value").isEqualTo(4919.0f)
                .jsonPath("$.curves.average_curve[0].date").isEqualTo("2022-03-04");
    }
}





















