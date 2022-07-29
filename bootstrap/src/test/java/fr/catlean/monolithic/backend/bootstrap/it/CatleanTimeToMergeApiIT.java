package fr.catlean.monolithic.backend.bootstrap.it;

import fr.catlean.monolithic.backend.domain.exception.CatleanExceptionCode;
import fr.catlean.monolithic.backend.domain.helper.DateHelper;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.Team;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.service.insights.PullRequestHistogramService;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.OnboardingEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.UserEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.PullRequestEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.OrganizationMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestRepository;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;


public class CatleanTimeToMergeApiIT extends AbstractCatleanMonolithicBackendIT {
    @Autowired
    public PullRequestHistogramService pullRequestHistogramService;
    @Autowired
    public PullRequestRepository pullRequestRepository;
    @Autowired
    public OrganizationRepository organizationRepository;
    @Autowired
    public UserRepository userRepository;
    private Organization organization;
    private User activeUser;
    private final List<Date> dates = DateHelper.getWeekStartDateForTheLastWeekNumber(3 * 4,
            TimeZone.getTimeZone(ZoneId.systemDefault()));
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");


    @Test
    void should_return_an_error_for_team_not_existing() {
        // Given
        final UUID teamId = UUID.randomUUID();
        // When
        client.get()
                .uri(getApiURI(TEAMS_GOALS_REST_API_TIME_TO_MERGE_HISTOGRAM, "team_id", teamId.toString()))
                .exchange()
                // Then
                .expectStatus()
                .is5xxServerError()
                .expectBody()
                .jsonPath("$.errors[0].code").isEqualTo(CatleanExceptionCode.TEAM_NOT_FOUND)
                .jsonPath("$.errors[0].message").isEqualTo(String.format("Team not found for id %s",
                        teamId));
    }

    @Order(1)
    @Test
    void should_get_time_to_merge_histogram_for_all_teams() {
        // Given
        final OrganizationEntity organizationEntity = organizationRepository.save(
                OrganizationEntity.builder()
                        .id(UUID.randomUUID())
                        .name(faker.rickAndMorty().character())
                        .build()
        );
        organization = OrganizationMapper.entityToDomain(organizationEntity);
        activeUser = UserMapper.entityToDomain(userRepository.save(
                UserEntity.builder()
                        .id(UUID.randomUUID())
                        .onboardingEntity(OnboardingEntity.builder().id(UUID.randomUUID()).hasConfiguredTeam(true).hasConnectedToVcs(true).build())
                        .organizationEntities(List.of(organizationEntity))
                        .status(User.ACTIVE)
                        .email(faker.gameOfThrones().character())
                        .build()
        ));
        authenticationContextProvider.authorizeUserForMail(activeUser.getEmail());
        pullRequestRepository.saveAll(generatePullRequestsStubsForOrganization(organization));


        // When
        client.get()
                .uri(getApiURI(TEAMS_GOALS_REST_API_TIME_TO_MERGE_HISTOGRAM))
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

    @Test
    void should_get_time_to_merge_curves_for_all_teams() {
        // When
        client.get()
                .uri(getApiURI(TEAMS_GOALS_REST_API_TIME_TO_MERGE_CURVES))
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

    private static List<PullRequestEntity> generatePullRequestsStubsForOrganization(final Organization organization) {
        final java.util.Date weekStartDate = DateHelper.getWeekStartDate(organization.getTimeZone());
        final Team teamAll = Team.buildTeamAll(organization.getId());
        final ArrayList<PullRequestEntity> pullRequests = new ArrayList<>();
        for (int i = 0; i < 7; i++) {

            pullRequests.add(PullRequestEntity.builder()
                    .id("pr-1-" + i)
                    .creationDate(
                            weekStartDate.toInstant()
                                    .atZone(organization.getTimeZone().toZoneId())
                                    .minus(i * 8, ChronoUnit.DAYS))
                    .addedLineNumber(800)
                    .deletedLineNumber(900)
                    .commitNumber(0)
                    .state(PullRequest.OPEN)
                    .startDateRange("01/06/2022")
                    .isMerged(false)
                    .daysOpened(i)
                    .title(faker.name().title())
                    .vcsOrganization(organization.getName())
                    .isDraft(false)
                    .vcsUrl(faker.pokemon().name())
                    .organizationId(organization.getId())
                    .authorLogin(faker.dragonBall().character())
                    .lastUpdateDate(ZonedDateTime.now())
                    .build());
            pullRequests.add(PullRequestEntity.builder()
                    .id("pr-2-" + i)
                    .creationDate(
                            weekStartDate.toInstant()
                                    .atZone(organization.getTimeZone().toZoneId())
                                    .minus(i * 8, ChronoUnit.DAYS))
                    .mergeDate(
                            weekStartDate.toInstant()
                                    .atZone(organization.getTimeZone().toZoneId())
                                    .minus(i * 8, ChronoUnit.DAYS))
                    .addedLineNumber(0)
                    .deletedLineNumber(300)
                    .commitNumber(0)
                    .daysOpened(i + 1)
                    .state(PullRequest.MERGE)
                    .isMerged(true)
                    .title(faker.name().title())
                    .vcsOrganization(organization.getName())
                    .isDraft(false)
                    .vcsUrl(faker.pokemon().name())
                    .organizationId(organization.getId())
                    .authorLogin(faker.dragonBall().character())
                    .lastUpdateDate(ZonedDateTime.now())
                    .startDateRange("01/07/2022")
                    .build());
            pullRequests.add(PullRequestEntity.builder()
                    .id("pr-3-" + i)
                    .creationDate(
                            weekStartDate.toInstant()
                                    .atZone(organization.getTimeZone().toZoneId())
                                    .minus(i * 8, ChronoUnit.DAYS))
                    .mergeDate(
                            weekStartDate.toInstant()
                                    .atZone(organization.getTimeZone().toZoneId())
                                    .minus(8, ChronoUnit.DAYS))
                    .addedLineNumber(500)
                    .deletedLineNumber(500)
                    .commitNumber(0)
                    .daysOpened(i + 2)
                    .startDateRange("01/06/2022")
                    .state(PullRequest.MERGE)
                    .isMerged(true)
                    .title(faker.name().title())
                    .vcsOrganization(organization.getName())
                    .isDraft(false)
                    .vcsUrl(faker.pokemon().name())
                    .organizationId(organization.getId())
                    .authorLogin(faker.dragonBall().character())
                    .lastUpdateDate(ZonedDateTime.now())
                    .build());
        }
        return pullRequests;

    }
}
