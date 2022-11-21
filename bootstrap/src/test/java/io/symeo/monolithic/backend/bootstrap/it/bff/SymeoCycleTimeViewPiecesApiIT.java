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

import static io.symeo.monolithic.backend.domain.helper.DateHelper.*;
import static java.time.ZonedDateTime.ofInstant;

public class SymeoCycleTimeViewPiecesApiIT extends AbstractSymeoBackForFrontendApiIT {

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
    private static final String repositoryId = faker.gameOfThrones().character();
    private static final String repositoryName = faker.name().lastName();
    private static final UUID teamId = UUID.randomUUID();
    private static final Integer pageIndex = 0;
    private static final Integer pageSize = 5;
    private static final String sortBy = "cycle_time";
    private static final String sortDir = "asc";

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
    void should_not_compute_cycle_time_curve_for_no_data_on_time_range() {
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
                .jsonPath("$.curves.piece_curve").isEmpty()
                .jsonPath("$.curves.average_curve").isEmpty();
    }

    @Order(3)
    @Test
    void should_compute_cycle_time_pieces() throws SymeoException {
        // Given
        final CycleTimeEntity inRangeCycleTimeEntity1 = CycleTimeEntity.builder()
                .id(faker.dragonBall().character() + "-current-1")
                .value(100L)
                .codingTime(200L)
                .reviewTime(300L)
                .timeToDeploy(400L)
                .deployDate(stringToDate("2022-02-15"))
                .pullRequestId(faker.rickAndMorty().character() + "-current-1")
                .pullRequestAuthorLogin(faker.name().firstName())
                .pullRequestMergeDate(stringToDate("2022-02-13"))
                .pullRequestCreationDate(stringToDate("2022-02-10"))
                .pullRequestUpdateDate(stringToDate("2022-02-13"))
                .pullRequestVcsRepositoryId(repositoryId)
                .pullRequestVcsRepository(faker.howIMetYourMother().character())
                .pullRequestVcsUrl(faker.backToTheFuture().character())
                .pullRequestState("merge")
                .pullRequestTitle(faker.harryPotter().character())
                .pullRequestHead("feature/test-current-1")
                .build();
        final CycleTimeEntity inRangeCycleTimeEntity2 = CycleTimeEntity.builder()
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

        final CycleTimeEntity outRangeCycleTimeEntity1 = CycleTimeEntity.builder()
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
        final CycleTimeEntity outRangeCycleTimeEntity2 = CycleTimeEntity.builder()
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
                inRangeCycleTimeEntity1, inRangeCycleTimeEntity2, outRangeCycleTimeEntity1, outRangeCycleTimeEntity2
        ));

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
                .jsonPath("$.pieces_page.pieces[0].id").isEqualTo(inRangeCycleTimeEntity1.getPullRequestId())
                .jsonPath("$.pieces_page.pieces[0].status").isEqualTo("merge")
                .jsonPath("$.pieces_page.pieces[0].title").isEqualTo(inRangeCycleTimeEntity1.getPullRequestTitle())
                .jsonPath("$.pieces_page.pieces[0].vcs_url").isEqualTo(inRangeCycleTimeEntity1.getPullRequestVcsUrl())
                .jsonPath("$.pieces_page.pieces[0].author").isEqualTo(inRangeCycleTimeEntity1.getPullRequestAuthorLogin())
                .jsonPath("$.pieces_page.pieces[0].vcs_repository").isEqualTo(inRangeCycleTimeEntity1.getPullRequestVcsRepository())
                .jsonPath("$.pieces_page.pieces[0].creation_date").isEqualTo("2022-02-10 00:00:00")
                .jsonPath("$.pieces_page.pieces[0].merge_date").isEqualTo("2022-02-13 00:00:00")
                .jsonPath("$.pieces_page.pieces[0].coding_time").isEqualTo(String.valueOf(inRangeCycleTimeEntity1.getCodingTime()))
                .jsonPath("$.pieces_page.pieces[0].review_time").isEqualTo(String.valueOf(inRangeCycleTimeEntity1.getReviewTime()))
                .jsonPath("$.pieces_page.pieces[0].time_to_deploy").isEqualTo(String.valueOf(inRangeCycleTimeEntity1.getTimeToDeploy()))
                .jsonPath("$.pieces_page.pieces[0].cycle_time").isEqualTo(String.valueOf(inRangeCycleTimeEntity1.getValue()))
                .jsonPath("$.pieces_page.pieces[1].id").isEqualTo(inRangeCycleTimeEntity2.getPullRequestId())
                .jsonPath("$.pieces_page.pieces[1].status").isEqualTo("merge")
                .jsonPath("$.pieces_page.pieces[1].title").isEqualTo(inRangeCycleTimeEntity2.getPullRequestTitle())
                .jsonPath("$.pieces_page.pieces[1].vcs_url").isEqualTo(inRangeCycleTimeEntity2.getPullRequestVcsUrl())
                .jsonPath("$.pieces_page.pieces[1].author").isEqualTo(inRangeCycleTimeEntity2.getPullRequestAuthorLogin())
                .jsonPath("$.pieces_page.pieces[1].vcs_repository").isEqualTo(inRangeCycleTimeEntity2.getPullRequestVcsRepository())
                .jsonPath("$.pieces_page.pieces[1].creation_date").isEqualTo("2022-02-09 00:00:00")
                .jsonPath("$.pieces_page.pieces[1].merge_date").isEqualTo("2022-02-17 00:00:00")
                .jsonPath("$.pieces_page.pieces[1].coding_time").isEqualTo(String.valueOf(inRangeCycleTimeEntity2.getCodingTime()))
                .jsonPath("$.pieces_page.pieces[1].review_time").isEqualTo(String.valueOf(inRangeCycleTimeEntity2.getReviewTime()))
                .jsonPath("$.pieces_page.pieces[1].time_to_deploy").isEqualTo(String.valueOf(inRangeCycleTimeEntity2.getTimeToDeploy()))
                .jsonPath("$.pieces_page.pieces[1].cycle_time").isEqualTo(String.valueOf(inRangeCycleTimeEntity2.getValue()));
    }

    @Order(4)
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
                .jsonPath("$.curves.average_curve[0].value").isEqualTo(100.0f)
                .jsonPath("$.curves.average_curve[0].date").isEqualTo("2022-02-15")
                .jsonPath("$.curves.average_curve[1].value").isEqualTo(200.0f)
                .jsonPath("$.curves.average_curve[1].date").isEqualTo("2022-02-20");
    }
}





















