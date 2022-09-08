package io.symeo.monolithic.backend.bootstrap.it.bff;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.account.TeamStandard;
import io.symeo.monolithic.backend.domain.model.account.User;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import io.symeo.monolithic.backend.domain.service.insights.PullRequestHistogramService;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.*;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.PullRequestEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.RepositoryEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.OrganizationMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.TeamGoalRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.RepositoryRepository;
import lombok.NonNull;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.assertj.core.api.Assertions.assertThat;


public class SymeoPullRequestStandardsApiIT extends AbstractSymeoBackForFrontendApiIT {
    @Autowired
    public PullRequestHistogramService pullRequestHistogramService;
    @Autowired
    public PullRequestRepository pullRequestRepository;
    @Autowired
    public OrganizationRepository organizationRepository;
    @Autowired
    public UserRepository userRepository;
    @Autowired
    public TeamRepository teamRepository;
    @Autowired
    public RepositoryRepository repositoryRepository;
    @Autowired
    public TeamGoalRepository teamGoalRepository;
    private static final UUID organizationId = UUID.randomUUID();
    private static final UUID activeUserId = UUID.randomUUID();
    private final static UUID currentTeamId = UUID.randomUUID();
    private final static String vcsUrl = faker.ancient().god();
    private final static String branchName = faker.ancient().hero();

    @Order(1)
    @Test
    void should_call_time_to_merge_api_and_return_an_error_for_team_not_existing() {
        // Given
        final OrganizationEntity organizationEntity = organizationRepository.save(
                OrganizationEntity.builder()
                        .id(organizationId)
                        .name(faker.rickAndMorty().character())
                        .build()
        );
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
        final UUID teamId = UUID.randomUUID();
        final String startDate = "2022-01-01";
        final String endDate = "2022-02-01";

        // When
        client.get()
                .uri(getApiURI(TEAMS_GOALS_REST_API_TIME_TO_MERGE_HISTOGRAM, getParams(teamId, startDate, endDate)))
                .exchange()
                // Then
                .expectStatus()
                .is5xxServerError()
                .expectBody()
                .jsonPath("$.errors[0].code").isEqualTo(SymeoExceptionCode.TEAM_NOT_FOUND)
                .jsonPath("$.errors[0].message").isEqualTo(String.format("Team not found for id %s",
                        teamId));
    }

    @Order(2)
    @Test
    void should_get_time_to_merge_histogram_given_a_team_id() throws SymeoException {
        // Given
        final List<RepositoryEntity> repositoryEntities = generateRepositoriesStubsForOrganization();
        repositoryRepository.saveAll(repositoryEntities);
        teamRepository.save(
                TeamEntity.builder().id(currentTeamId).name(faker.dragonBall().character())
                        .organizationId(organizationId).repositoryIds(List.of(repositoryEntities.get(0).getId())).build());
        teamGoalRepository.save(TeamGoalEntity.builder().teamId(currentTeamId).id(UUID.randomUUID()).standardCode(TeamStandard.TIME_TO_MERGE).value("5").build());
        final String startDate = "2022-01-01";
        final String endDate = "2022-03-01";
        pullRequestRepository.saveAll(
                generatePullRequestsStubsForOrganization(OrganizationMapper.entityToDomain(organizationRepository
                        .findById(organizationId).get()), startDate));

        // When
        client.get()
                .uri(getApiURI(TEAMS_GOALS_REST_API_TIME_TO_MERGE_HISTOGRAM, getParams(currentTeamId, startDate,
                        endDate)))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.histogram.limit").isEqualTo(5)
                .jsonPath("$.histogram.data[0].data_above_limit").isEqualTo(1)
                .jsonPath("$.histogram.data[0].data_below_limit").isEqualTo(2)
                .jsonPath("$.histogram.data[0].start_date_range").isEqualTo("2022-01-01")
                .jsonPath("$.histogram.data[1].data_above_limit").isEqualTo(1)
                .jsonPath("$.histogram.data[1].data_below_limit").isEqualTo(2)
                .jsonPath("$.histogram.data[1].start_date_range").isEqualTo("2022-01-08")
                .jsonPath("$.histogram.data[2].data_above_limit").isEqualTo(2)
                .jsonPath("$.histogram.data[2].data_below_limit").isEqualTo(2)
                .jsonPath("$.histogram.data[2].start_date_range").isEqualTo("2022-01-15")
                .jsonPath("$.histogram.data[3].data_above_limit").isEqualTo(3)
                .jsonPath("$.histogram.data[3].data_below_limit").isEqualTo(2)
                .jsonPath("$.histogram.data[3].start_date_range").isEqualTo("2022-01-22");
    }

    @Order(3)
    @Test
    void should_get_time_to_merge_curves_given_a_team_id() {
        // Given
        final String startDate = "2022-01-01";
        final String endDate = "2022-02-01";

        // When
        client.get()
                .uri(getApiURI(TEAMS_GOALS_REST_API_TIME_TO_MERGE_CURVES, getParams(currentTeamId, startDate, endDate)))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.curves.limit").isEqualTo(5)
                .jsonPath("$.curves.average_curve[0].value").isEqualTo(2)
                .jsonPath("$.curves.average_curve[0].date").isEqualTo("2022-01-19")
                .jsonPath("$.curves.average_curve[1].value").isEqualTo(0)
                .jsonPath("$.curves.average_curve[1].date").isEqualTo("2022-01-09")
                .jsonPath("$.curves.average_curve[2].value").isEqualTo(0)
                .jsonPath("$.curves.average_curve[2].date").isEqualTo("2022-01-25")
                .jsonPath("$.curves.average_curve[3].value").isEqualTo(0)
                .jsonPath("$.curves.average_curve[3].date").isEqualTo("2022-01-17")
                .jsonPath("$.curves.piece_curve[0].date").isEqualTo("2022-02-01")
                .jsonPath("$.curves.piece_curve[0].link").isEqualTo(vcsUrl)
                .jsonPath("$.curves.piece_curve[0].label").isEqualTo(branchName)
                .jsonPath("$.curves.piece_curve[1].date").isEqualTo("2022-01-01")
                .jsonPath("$.curves.piece_curve[1].link").isEqualTo(vcsUrl)
                .jsonPath("$.curves.piece_curve[1].label").isEqualTo(branchName)
                .jsonPath("$.curves.piece_curve[2].value").isEqualTo(0)
                .jsonPath("$.curves.piece_curve[2].date").isEqualTo("2022-01-01")
                .jsonPath("$.curves.piece_curve[2].link").isEqualTo(vcsUrl)
                .jsonPath("$.curves.piece_curve[2].label").isEqualTo(branchName);
    }


    @Order(4)
    @Test
    void should_call_pull_request_size_api_and_return_an_error_for_team_not_existing() {
        // Given
        final OrganizationEntity organizationEntity = organizationRepository.save(
                OrganizationEntity.builder()
                        .id(organizationId)
                        .name(faker.rickAndMorty().character())
                        .build()
        );
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
        final UUID teamId = UUID.randomUUID();
        final String startDate = "2022-01-01";
        final String endDate = "2022-03-01";

        // When
        client.get()
                .uri(getApiURI(TEAMS_GOALS_REST_API_PULL_REQUEST_SIZE_CURVES, getParams(teamId, startDate, endDate)))
                .exchange()
                // Then
                .expectStatus()
                .is5xxServerError()
                .expectBody()
                .jsonPath("$.errors[0].code").isEqualTo(SymeoExceptionCode.TEAM_NOT_FOUND)
                .jsonPath("$.errors[0].message").isEqualTo(String.format("Team not found for id %s",
                        teamId));
    }


    @Order(5)
    @Test
    void should_get_pull_request_size_curves_given_a_team_id() {
        // Given
        teamGoalRepository.save(TeamGoalEntity.builder().teamId(currentTeamId).id(UUID.randomUUID()).standardCode(TeamStandard.PULL_REQUEST_SIZE).value("1000").build());
        final String startDate = "2022-01-01";
        final String endDate = "2022-03-01";

        // When
        client.get()
                .uri(getApiURI(TEAMS_GOALS_REST_API_PULL_REQUEST_SIZE_CURVES, getParams(currentTeamId, startDate,
                        endDate)))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.curves.limit").isEqualTo(1000)
                .jsonPath("$.curves.average_curve[0].value").isEqualTo(1000)
                .jsonPath("$.curves.average_curve[0].date").isEqualTo("2022-01-19")
                .jsonPath("$.curves.average_curve[1].value").isEqualTo(300)
                .jsonPath("$.curves.average_curve[1].date").isEqualTo("2022-01-09")
                .jsonPath("$.curves.average_curve[2].value").isEqualTo(300)
                .jsonPath("$.curves.average_curve[2].date").isEqualTo("2022-01-25")
                .jsonPath("$.curves.average_curve[3].value").isEqualTo(1000)
                .jsonPath("$.curves.average_curve[3].date").isEqualTo("2022-02-15")
                .jsonPath("$.curves.average_curve[4].value").isEqualTo(300)
                .jsonPath("$.curves.piece_curve[0].value").isEqualTo(1700)
                .jsonPath("$.curves.piece_curve[0].date").isEqualTo("2022-03-01")
                .jsonPath("$.curves.piece_curve[0].link").isEqualTo(vcsUrl)
                .jsonPath("$.curves.piece_curve[0].label").isEqualTo(branchName)
                .jsonPath("$.curves.piece_curve[1].value").isEqualTo(300)
                .jsonPath("$.curves.piece_curve[1].date").isEqualTo("2022-01-01")
                .jsonPath("$.curves.piece_curve[1].link").isEqualTo(vcsUrl)
                .jsonPath("$.curves.piece_curve[1].label").isEqualTo(branchName)
                .jsonPath("$.curves.piece_curve[2].value").isEqualTo(1000)
                .jsonPath("$.curves.piece_curve[2].date").isEqualTo("2022-01-01")
                .jsonPath("$.curves.piece_curve[2].link").isEqualTo(vcsUrl)
                .jsonPath("$.curves.piece_curve[2].label").isEqualTo(branchName);


    }

    @Order(6)
    @Test
    void should_get_pull_request_size_histogram_given_a_team_id() {
        // Given
        final String startDate = "2022-01-01";
        final String endDate = "2022-03-01";

        // When
        client.get()
                .uri(getApiURI(TEAMS_GOALS_REST_API_PULL_REQUEST_SIZE_HISTOGRAM, getParams(currentTeamId, startDate,
                        endDate)))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.histogram.limit").isEqualTo(1000)
                .jsonPath("$.histogram.data[0].data_above_limit").isEqualTo(2)
                .jsonPath("$.histogram.data[0].data_below_limit").isEqualTo(1)
                .jsonPath("$.histogram.data[0].start_date_range").isEqualTo("2022-01-01")
                .jsonPath("$.histogram.data[1].data_above_limit").isEqualTo(2)
                .jsonPath("$.histogram.data[1].data_below_limit").isEqualTo(1)
                .jsonPath("$.histogram.data[1].start_date_range").isEqualTo("2022-01-08")
                .jsonPath("$.histogram.data[2].data_above_limit").isEqualTo(3)
                .jsonPath("$.histogram.data[2].data_below_limit").isEqualTo(1)
                .jsonPath("$.histogram.data[2].start_date_range").isEqualTo("2022-01-15")
                .jsonPath("$.histogram.data[3].data_above_limit").isEqualTo(4)
                .jsonPath("$.histogram.data[3].data_below_limit").isEqualTo(1)
                .jsonPath("$.histogram.data[3].start_date_range").isEqualTo("2022-01-22")
                .jsonPath("$.histogram.data[4].data_above_limit").isEqualTo(4)
                .jsonPath("$.histogram.data[4].data_below_limit").isEqualTo(1)
                .jsonPath("$.histogram.data[4].start_date_range").isEqualTo("2022-01-29")
                .jsonPath("$.histogram.data[5].data_above_limit").isEqualTo(6)
                .jsonPath("$.histogram.data[5].data_below_limit").isEqualTo(1)
                .jsonPath("$.histogram.data[5].start_date_range").isEqualTo("2022-02-05")
                .jsonPath("$.histogram.data[6].data_above_limit").isEqualTo(7)
                .jsonPath("$.histogram.data[6].data_below_limit").isEqualTo(1)
                .jsonPath("$.histogram.data[6].start_date_range").isEqualTo("2022-02-12")
                .jsonPath("$.histogram.data[7].data_above_limit").isEqualTo(8)
                .jsonPath("$.histogram.data[7].data_below_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[7].start_date_range").isEqualTo("2022-02-19")
                .jsonPath("$.histogram.data[8].data_above_limit").isEqualTo(7)
                .jsonPath("$.histogram.data[8].data_below_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[8].start_date_range").isEqualTo("2022-02-26")
                .jsonPath("$.histogram.data[9].data_above_limit").isEqualTo(7)
                .jsonPath("$.histogram.data[9].data_below_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[9].start_date_range").isEqualTo("2022-03-01");
    }

    @Order(7)
    @Test
    public void should_get_pull_request_size_metrics_given_a_team_id() {
        // Given
        final String startDate = "2022-01-15";
        final String endDate = "2022-02-01";

        // When
        client.get()
                .uri(getApiURI(TEAMS_GOALS_REST_API_PULL_REQUEST_SIZE_METRICS, getParams(currentTeamId, startDate,
                        endDate)))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.metrics.average.value").isEqualTo(1175.0)
                .jsonPath("$.metrics.average.tendency_percentage").isEqualTo(17.5)
                .jsonPath("$.metrics.meeting_goal.value").isEqualTo(50.0)
                .jsonPath("$.metrics.meeting_goal.tendency_percentage").isEqualTo(-25.0);
    }

    @Order(8)
    @Test
    public void should_get_time_to_merge_metrics_given_a_team_id() {
        // Given
        final String startDate = "2022-01-15";
        final String endDate = "2022-02-01";

        // When
        client.get()
                .uri(getApiURI(TEAMS_GOALS_REST_API_TIME_TO_MERGE_METRICS, getParams(currentTeamId, startDate,
                        endDate)))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.metrics.average.value").isEqualTo(10.2)
                .jsonPath("$.metrics.average.tendency_percentage").isEqualTo(183.3)
                .jsonPath("$.metrics.meeting_goal.value").isEqualTo(50.0)
                .jsonPath("$.metrics.meeting_goal.tendency_percentage").isEqualTo(-25.0);
    }

    @Order(9)
    @Test
    void should_raise_an_exception_for_invalid_page_size() {
        // Given
        final String startDate = "2022-01-15";
        final String endDate = "2022-02-01";
        final String pageIndex = "0";
        final String pageSize = "100000";
        final String sortingDirection = "asc";
        final String sortingParameter = "creation_date";

        // When
        client.get()
                .uri(getApiURI(TEAMS_REST_API_PULL_REQUESTS, getParams(currentTeamId, startDate,
                        endDate, pageIndex, pageSize, sortingParameter, sortingDirection)))
                .exchange()
                // Then
                .expectStatus()
                .is5xxServerError()
                .expectBody()
                .jsonPath("$.pull_requests_page").isEmpty()
                .jsonPath("$.errors[0].code").isEqualTo(SymeoExceptionCode.PAGINATION_MAXIMUM_SIZE_EXCEEDED);
    }


    @Test
    void should_return_pull_requests_pages() {
        // Given
        final String startDate = "2022-01-15";
        final String endDate = "2022-02-01";
        String pageIndex = "0";
        final String pageSize = "5";
        final String sortingDirection = "asc";
        final String sortingParameter = "creation_date";

        // When
        client.get()
                .uri(getApiURI(TEAMS_REST_API_PULL_REQUESTS, getParams(currentTeamId, startDate,
                        endDate, pageIndex, pageSize, sortingParameter, sortingDirection)))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.pull_requests_page.total_page_number").isEqualTo(2)
                .jsonPath("$.pull_requests_page.total_item_number").isEqualTo(8)
                .jsonPath("$.pull_requests_page.pull_requests").isNotEmpty()
                .jsonPath("$.pull_requests_page.pull_requests").value((List<Object> o) -> assertThat(o).hasSize(5))
                .jsonPath("$.pull_requests_page.pull_requests[0].id").isNotEmpty()
                .jsonPath("$.pull_requests_page.pull_requests[0].commit_number").isNotEmpty()
                .jsonPath("$.pull_requests_page.pull_requests[0].size").isNotEmpty()
                .jsonPath("$.pull_requests_page.pull_requests[0].days_opened").isNotEmpty()
                .jsonPath("$.pull_requests_page.pull_requests[0].creation_date").isNotEmpty()
                .jsonPath("$.pull_requests_page.pull_requests[0].status").isNotEmpty()
                .jsonPath("$.pull_requests_page.pull_requests[0].vcs_url").isNotEmpty()
                .jsonPath("$.pull_requests_page.pull_requests[0].title").isNotEmpty()
                .jsonPath("$.pull_requests_page.pull_requests[0].author").isNotEmpty()
                .jsonPath("$.pull_requests_page.pull_requests[0].vcs_repository").isNotEmpty();

        pageIndex = "1";
        client.get()
                .uri(getApiURI(TEAMS_REST_API_PULL_REQUESTS, getParams(currentTeamId, startDate,
                        endDate, pageIndex, pageSize, sortingParameter, sortingDirection)))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.pull_requests_page.total_page_number").isEqualTo(2)
                .jsonPath("$.pull_requests_page.total_item_number").isEqualTo(8)
                .jsonPath("$.pull_requests_page.pull_requests").isNotEmpty()
                .jsonPath("$.pull_requests_page.pull_requests").value((List<Object> o) -> assertThat(o).hasSize(3));
    }

    private static List<RepositoryEntity> generateRepositoriesStubsForOrganization() {
        return List.of(
                RepositoryEntity.builder().vcsOrganizationName(faker.rickAndMorty().character())
                        .name(faker.name().firstName()).organizationId(organizationId)
                        .id("repository-1")
                        .build(),
                RepositoryEntity.builder().vcsOrganizationName(faker.rickAndMorty().character())
                        .name(faker.name().firstName()).organizationId(organizationId)
                        .id("repository-2")
                        .build()
        );
    }


    @NonNull
    private static Map<String, String> getParams(UUID teamId, String startDate, String endDate) {
        return Map.of("team_id", teamId.toString(),
                "start_date", startDate, "end_date", endDate);
    }

    @NonNull
    private static Map<String, String> getParams(UUID teamId, String startDate, String endDate, String pageIndex,
                                                 String pageSize, String sortingParameter, String sortingDirection) {
        return Map.of("team_id", teamId.toString(),
                "start_date", startDate, "end_date", endDate, "page_index", pageIndex, "page_size", pageSize,
                "sort_by", sortingParameter, "sort_dir", sortingDirection);
    }


    private static List<PullRequestEntity> generatePullRequestsStubsForOrganization(final Organization organization,
                                                                                    final String startDate) throws SymeoException {
        final java.util.Date weekStartDate = stringToDate(startDate);
        final ArrayList<PullRequestEntity> pullRequests = new ArrayList<>();
        for (int i = 0; i < 7; i++) {

            pullRequests.add(PullRequestEntity.builder()
                    .id("pr-1-" + i)
                    .creationDate(
                            weekStartDate.toInstant()
                                    .atZone(organization.getTimeZone().toZoneId())
                                    .plus(i * 8, ChronoUnit.DAYS))
                    .addedLineNumber(800)
                    .code(faker.dragonBall().character())
                    .deletedLineNumber(900)
                    .commitNumber(0)
                    .state(PullRequest.OPEN)
                    .isMerged(false)
                    .title(faker.name().title())
                    .vcsOrganizationId(organization.getName())
                    .vcsRepository(faker.ancient().primordial())
                    .isDraft(false)
                    .vcsUrl(vcsUrl)
                    .organizationId(organization.getId())
                    .authorLogin(faker.dragonBall().character())
                    .vcsRepositoryId("repository-1")
                    .lastUpdateDate(ZonedDateTime.now())
                    .branchName(branchName)
                    .build());
            pullRequests.add(PullRequestEntity.builder()
                    .id("pr-2-" + i)
                    .creationDate(
                            weekStartDate.toInstant()
                                    .atZone(organization.getTimeZone().toZoneId())
                                    .plus(i * 8, ChronoUnit.DAYS))
                    .mergeDate(
                            weekStartDate.toInstant()
                                    .atZone(organization.getTimeZone().toZoneId())
                                    .plus(i * 8, ChronoUnit.DAYS))
                    .addedLineNumber(0)
                    .deletedLineNumber(300)
                    .commitNumber(0)
                    .code(faker.dragonBall().character())
                    .vcsRepositoryId("repository-1")
                    .state(PullRequest.MERGE)
                    .isMerged(true)
                    .title(faker.name().title())
                    .vcsRepository(faker.ancient().primordial())
                    .vcsOrganizationId(organization.getName())
                    .isDraft(false)
                    .vcsUrl(vcsUrl)
                    .organizationId(organization.getId())
                    .authorLogin(faker.dragonBall().character())
                    .lastUpdateDate(ZonedDateTime.now())
                    .branchName(branchName)
                    .build());
            pullRequests.add(PullRequestEntity.builder()
                    .id("pr-3-" + i)
                    .creationDate(
                            weekStartDate.toInstant()
                                    .atZone(organization.getTimeZone().toZoneId())
                                    .plus(i * 8, ChronoUnit.DAYS))
                    .mergeDate(
                            weekStartDate.toInstant()
                                    .atZone(organization.getTimeZone().toZoneId())
                                    .plus(i * 9, ChronoUnit.DAYS))
                    .addedLineNumber(500)
                    .code(faker.dragonBall().character())
                    .deletedLineNumber(500)
                    .commitNumber(0)
                    .state(PullRequest.MERGE)
                    .isMerged(true)
                    .title(faker.name().title())
                    .vcsRepository(faker.ancient().primordial())
                    .vcsOrganizationId(organization.getName())
                    .branchName(branchName)
                    .isDraft(false)
                    .vcsRepositoryId("repository-1")
                    .vcsUrl(vcsUrl)
                    .organizationId(organization.getId())
                    .authorLogin(faker.dragonBall().character())
                    .lastUpdateDate(ZonedDateTime.now())
                    .build());
            pullRequests.add(PullRequestEntity.builder()
                    .id("pr-4-" + i)
                    .creationDate(
                            weekStartDate.toInstant()
                                    .atZone(organization.getTimeZone().toZoneId())
                                    .plus(i * 8, ChronoUnit.DAYS))
                    .mergeDate(
                            weekStartDate.toInstant()
                                    .atZone(organization.getTimeZone().toZoneId())
                                    .plus(i * 9, ChronoUnit.DAYS))
                    .addedLineNumber(500)
                    .vcsRepository(faker.ancient().primordial())
                    .deletedLineNumber(500)
                    .commitNumber(0)
                    .state(PullRequest.MERGE)
                    .isMerged(true)
                    .code(faker.dragonBall().character())
                    .branchName(branchName)
                    .title(faker.name().title())
                    .vcsOrganizationId(organization.getName())
                    .isDraft(false)
                    .vcsRepositoryId("repository-2")
                    .vcsUrl(vcsUrl)
                    .organizationId(organization.getId())
                    .authorLogin(faker.dragonBall().character())
                    .lastUpdateDate(ZonedDateTime.now())
                    .build());
        }
        return pullRequests;

    }
}
