package fr.catlean.monolithic.backend.bootstrap.it.bff;

import fr.catlean.monolithic.backend.domain.exception.CatleanExceptionCode;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.TeamStandard;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.service.insights.PullRequestHistogramService;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.*;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.PullRequestEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.RepositoryEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.OrganizationMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.TeamGoalRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.RepositoryRepository;
import lombok.NonNull;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;


public class CatleanPullRequestStandardsApiIT extends AbstractCatleanBackForFrontendApiIT {
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
    private final List<Date> dates = new ArrayList<>();
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");


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
                .jsonPath("$.errors[0].code").isEqualTo(CatleanExceptionCode.TEAM_NOT_FOUND)
                .jsonPath("$.errors[0].message").isEqualTo(String.format("Team not found for id %s",
                        teamId));
    }

    @Order(2)
    @Test
    void should_get_time_to_merge_histogram_given_a_team_id() {
        // Given
        final List<RepositoryEntity> repositoryEntities = generateRepositoriesStubsForOrganization();
        repositoryRepository.saveAll(repositoryEntities);
        teamRepository.save(
                TeamEntity.builder().id(currentTeamId).name(faker.dragonBall().character())
                        .organizationId(organizationId).repositoryIds(List.of(repositoryEntities.get(0).getId())).build());
        teamGoalRepository.save(TeamGoalEntity.builder().teamId(currentTeamId).id(UUID.randomUUID()).standardCode(TeamStandard.TIME_TO_MERGE).value("5").build());
//        pullRequestRepository.saveAll(
//                generatePullRequestsStubsForOrganization(OrganizationMapper.entityToDomain(organizationRepository.findById(organizationId).get())));
        final String startDate = "2022-01-01";
        final String endDate = "2022-03-01";

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
                .jsonPath("$.histogram.data[0].data_above_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[0].data_below_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[0].start_date_range").isEqualTo(simpleDateFormat.format(dates.get(0)))
                .jsonPath("$.histogram.data[1].data_above_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[1].data_below_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[1].start_date_range").isEqualTo(simpleDateFormat.format(dates.get(1)))
                .jsonPath("$.histogram.data[2].data_above_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[2].data_below_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[2].start_date_range").isEqualTo(simpleDateFormat.format(dates.get(2)))
                .jsonPath("$.histogram.data[3].data_above_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[3].data_below_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[3].start_date_range").isEqualTo(simpleDateFormat.format(dates.get(3)))
                .jsonPath("$.histogram.data[4].data_above_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[4].data_below_limit").isEqualTo(1)
                .jsonPath("$.histogram.data[4].start_date_range").isEqualTo(simpleDateFormat.format(dates.get(4)))
                .jsonPath("$.histogram.data[5].data_above_limit").isEqualTo(2)
                .jsonPath("$.histogram.data[5].data_below_limit").isEqualTo(1)
                .jsonPath("$.histogram.data[5].start_date_range").isEqualTo(simpleDateFormat.format(dates.get(5)))
                .jsonPath("$.histogram.data[6].data_above_limit").isEqualTo(2)
                .jsonPath("$.histogram.data[6].data_below_limit").isEqualTo(3)
                .jsonPath("$.histogram.data[6].start_date_range").isEqualTo(simpleDateFormat.format(dates.get(6)))
                .jsonPath("$.histogram.data[7].data_above_limit").isEqualTo(4)
                .jsonPath("$.histogram.data[7].data_below_limit").isEqualTo(3)
                .jsonPath("$.histogram.data[7].start_date_range").isEqualTo(simpleDateFormat.format(dates.get(7)))
                .jsonPath("$.histogram.data[8].data_above_limit").isEqualTo(6)
                .jsonPath("$.histogram.data[8].data_below_limit").isEqualTo(3)
                .jsonPath("$.histogram.data[8].start_date_range").isEqualTo(simpleDateFormat.format(dates.get(8)))
                .jsonPath("$.histogram.data[9].data_above_limit").isEqualTo(10)
                .jsonPath("$.histogram.data[9].data_below_limit").isEqualTo(2)
                .jsonPath("$.histogram.data[9].start_date_range").isEqualTo(simpleDateFormat.format(dates.get(9)))
                .jsonPath("$.histogram.data[10].data_above_limit").isEqualTo(5)
                .jsonPath("$.histogram.data[10].data_below_limit").isEqualTo(1)
                .jsonPath("$.histogram.data[10].start_date_range").isEqualTo(simpleDateFormat.format(dates.get(10)))
                .jsonPath("$.histogram.data[11].data_above_limit").isEqualTo(7)
                .jsonPath("$.histogram.data[11].data_below_limit").isEqualTo(2)
                .jsonPath("$.histogram.data[11].start_date_range").isEqualTo(simpleDateFormat.format(dates.get(11)));
    }

    @Order(3)
    @Test
    void should_get_time_to_merge_curves_given_a_team_id() {
        // Given
        final String startDate = "2022-01-01";
        final String endDate = "2022-03-01";

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
                .jsonPath("$.curves.average_curve[0].value").isEqualTo(4)
                .jsonPath("$.curves.average_curve[0].date").isEqualTo("01/07/2022")
                .jsonPath("$.curves.average_curve[1].value").isEqualTo(4)
                .jsonPath("$.curves.average_curve[1].date").isEqualTo("01/06/2022")
                .jsonPath("$.curves.piece_curve[0].date").isEqualTo("01/06/2022")
                .jsonPath("$.curves.piece_curve[0].value").isEqualTo(1)
                .jsonPath("$.curves.piece_curve[0].open").isEqualTo(true)
                .jsonPath("$.curves.piece_curve[1].value").isEqualTo(1)
                .jsonPath("$.curves.piece_curve[1].date").isEqualTo("01/07/2022")
                .jsonPath("$.curves.piece_curve[1].open").isEqualTo(false)
                .jsonPath("$.curves.piece_curve[2].value").isEqualTo(2)
                .jsonPath("$.curves.piece_curve[2].date").isEqualTo("01/06/2022")
                .jsonPath("$.curves.piece_curve[2].open").isEqualTo(false)
                .jsonPath("$.curves.piece_curve[3].value").isEqualTo(1)
                .jsonPath("$.curves.piece_curve[3].date").isEqualTo("01/06/2022")
                .jsonPath("$.curves.piece_curve[3].open").isEqualTo(true)
                .jsonPath("$.curves.piece_curve[4].value").isEqualTo(2)
                .jsonPath("$.curves.piece_curve[4].date").isEqualTo("01/07/2022")
                .jsonPath("$.curves.piece_curve[4].open").isEqualTo(false);


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
                .jsonPath("$.errors[0].code").isEqualTo(CatleanExceptionCode.TEAM_NOT_FOUND)
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
                .jsonPath("$.curves.average_curve[0].value").isEqualTo(300)
                .jsonPath("$.curves.average_curve[0].date").isEqualTo("01/07/2022")
                .jsonPath("$.curves.average_curve[1].value").isEqualTo(1600)
                .jsonPath("$.curves.average_curve[1].date").isEqualTo("01/06/2022")
                .jsonPath("$.curves.piece_curve[0].date").isEqualTo("01/06/2022")
                .jsonPath("$.curves.piece_curve[0].value").isEqualTo(1200)
                .jsonPath("$.curves.piece_curve[0].open").isEqualTo(true)
                .jsonPath("$.curves.piece_curve[1].value").isEqualTo(300)
                .jsonPath("$.curves.piece_curve[1].date").isEqualTo("01/07/2022")
                .jsonPath("$.curves.piece_curve[1].open").isEqualTo(false)
                .jsonPath("$.curves.piece_curve[2].value").isEqualTo(2000)
                .jsonPath("$.curves.piece_curve[2].date").isEqualTo("01/06/2022")
                .jsonPath("$.curves.piece_curve[2].open").isEqualTo(false)
                .jsonPath("$.curves.piece_curve[3].value").isEqualTo(1200)
                .jsonPath("$.curves.piece_curve[3].date").isEqualTo("01/06/2022")
                .jsonPath("$.curves.piece_curve[3].open").isEqualTo(true)
                .jsonPath("$.curves.piece_curve[4].value").isEqualTo(300)
                .jsonPath("$.curves.piece_curve[4].date").isEqualTo("01/07/2022")
                .jsonPath("$.curves.piece_curve[4].open").isEqualTo(false);


    }

    @Order(6)
    @Test
    void should_get_pull_request_size_histogram_given_a_team_id() {
        // Given
        final String startDate = "2022-01-01";
        final String endDate = "2022-03-01";

        // When
        client.get()
                .uri(getApiURI(TEAMS_GOALS_REST_API_PULL_REQUEST_SIZE_HISTOGRAM, getParams(currentTeamId,startDate,endDate)))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.histogram.limit").isEqualTo(1000)
                .jsonPath("$.histogram.data[0].data_above_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[0].data_below_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[0].start_date_range").isEqualTo(simpleDateFormat.format(dates.get(0)))
                .jsonPath("$.histogram.data[1].data_above_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[1].data_below_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[1].start_date_range").isEqualTo(simpleDateFormat.format(dates.get(1)))
                .jsonPath("$.histogram.data[2].data_above_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[2].data_below_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[2].start_date_range").isEqualTo(simpleDateFormat.format(dates.get(2)))
                .jsonPath("$.histogram.data[3].data_above_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[3].data_below_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[3].start_date_range").isEqualTo(simpleDateFormat.format(dates.get(3)))
                .jsonPath("$.histogram.data[4].data_above_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[4].data_below_limit").isEqualTo(1)
                .jsonPath("$.histogram.data[4].start_date_range").isEqualTo(simpleDateFormat.format(dates.get(4)))
                .jsonPath("$.histogram.data[5].data_above_limit").isEqualTo(2)
                .jsonPath("$.histogram.data[5].data_below_limit").isEqualTo(1)
                .jsonPath("$.histogram.data[5].start_date_range").isEqualTo(simpleDateFormat.format(dates.get(5)))
                .jsonPath("$.histogram.data[6].data_above_limit").isEqualTo(4)
                .jsonPath("$.histogram.data[6].data_below_limit").isEqualTo(1)
                .jsonPath("$.histogram.data[6].start_date_range").isEqualTo(simpleDateFormat.format(dates.get(6)))
                .jsonPath("$.histogram.data[7].data_above_limit").isEqualTo(6)
                .jsonPath("$.histogram.data[7].data_below_limit").isEqualTo(1)
                .jsonPath("$.histogram.data[7].start_date_range").isEqualTo(simpleDateFormat.format(dates.get(7)))
                .jsonPath("$.histogram.data[8].data_above_limit").isEqualTo(8)
                .jsonPath("$.histogram.data[8].data_below_limit").isEqualTo(1)
                .jsonPath("$.histogram.data[8].start_date_range").isEqualTo(simpleDateFormat.format(dates.get(8)))
                .jsonPath("$.histogram.data[9].data_above_limit").isEqualTo(11)
                .jsonPath("$.histogram.data[9].data_below_limit").isEqualTo(1)
                .jsonPath("$.histogram.data[9].start_date_range").isEqualTo(simpleDateFormat.format(dates.get(9)))
                .jsonPath("$.histogram.data[10].data_above_limit").isEqualTo(6)
                .jsonPath("$.histogram.data[10].data_below_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[10].start_date_range").isEqualTo(simpleDateFormat.format(dates.get(10)))
                .jsonPath("$.histogram.data[11].data_above_limit").isEqualTo(8)
                .jsonPath("$.histogram.data[11].data_below_limit").isEqualTo(1)
                .jsonPath("$.histogram.data[11].start_date_range").isEqualTo(simpleDateFormat.format(dates.get(11)));
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
}
