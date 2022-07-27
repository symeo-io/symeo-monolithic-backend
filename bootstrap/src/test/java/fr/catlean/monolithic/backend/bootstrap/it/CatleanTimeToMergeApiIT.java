package fr.catlean.monolithic.backend.bootstrap.it;

import fr.catlean.monolithic.backend.domain.helper.DateHelper;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.Team;
import fr.catlean.monolithic.backend.domain.model.account.User;
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

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


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
                .uri(getApiURI(TIME_TO_MERGE_REST_API_HISTOGRAM))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.histogram.data[0].data_above_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[0].data_below_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[0].start_date_range").isEqualTo("09/05/2022")
                .jsonPath("$.histogram.data[1].data_above_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[1].data_below_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[1].start_date_range").isEqualTo("16/05/2022")
                .jsonPath("$.histogram.data[2].data_above_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[2].data_below_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[2].start_date_range").isEqualTo("23/05/2022")
                .jsonPath("$.histogram.data[3].data_above_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[3].data_below_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[3].start_date_range").isEqualTo("30/05/2022")
                .jsonPath("$.histogram.data[4].data_above_limit").isEqualTo(1)
                .jsonPath("$.histogram.data[4].data_below_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[4].start_date_range").isEqualTo("06/06/2022")
                .jsonPath("$.histogram.data[5].data_above_limit").isEqualTo(3)
                .jsonPath("$.histogram.data[5].data_below_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[5].start_date_range").isEqualTo("13/06/2022")
                .jsonPath("$.histogram.data[6].data_above_limit").isEqualTo(5)
                .jsonPath("$.histogram.data[6].data_below_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[6].start_date_range").isEqualTo("20/06/2022")
                .jsonPath("$.histogram.data[7].data_above_limit").isEqualTo(7)
                .jsonPath("$.histogram.data[7].data_below_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[7].start_date_range").isEqualTo("27/06/2022")
                .jsonPath("$.histogram.data[8].data_above_limit").isEqualTo(9)
                .jsonPath("$.histogram.data[8].data_below_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[8].start_date_range").isEqualTo("04/07/2022")
                .jsonPath("$.histogram.data[9].data_above_limit").isEqualTo(12)
                .jsonPath("$.histogram.data[9].data_below_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[9].start_date_range").isEqualTo("11/07/2022")
                .jsonPath("$.histogram.data[10].data_above_limit").isEqualTo(6)
                .jsonPath("$.histogram.data[10].data_below_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[10].start_date_range").isEqualTo("18/07/2022")
                .jsonPath("$.histogram.data[11].data_above_limit").isEqualTo(9)
                .jsonPath("$.histogram.data[11].data_below_limit").isEqualTo(0)
                .jsonPath("$.histogram.data[11].start_date_range").isEqualTo("25/07/2022");
    }

    @Test
    void should_get_time_to_merge_curves_for_all_teams() {
        // When
        client.get()
                .uri(getApiURI(TIME_TO_MERGE_REST_API_CURVE))
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty();
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
                    .addedLineNumber(teamAll.getPullRequestLineNumberLimit() * 2)
                    .deletedLineNumber(teamAll.getPullRequestLineNumberLimit() * 2)
                    .commitNumber(0)
                    .isMerged(false)
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
                    .addedLineNumber(teamAll.getPullRequestLineNumberLimit() * 2)
                    .deletedLineNumber(teamAll.getPullRequestLineNumberLimit() * 2)
                    .commitNumber(0)
                    .isMerged(true)
                    .title(faker.name().title())
                    .vcsOrganization(organization.getName())
                    .isDraft(false)
                    .vcsUrl(faker.pokemon().name())
                    .organizationId(organization.getId())
                    .authorLogin(faker.dragonBall().character())
                    .lastUpdateDate(ZonedDateTime.now())
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
                    .addedLineNumber(teamAll.getPullRequestLineNumberLimit() * 2)
                    .deletedLineNumber(teamAll.getPullRequestLineNumberLimit() * 2)
                    .commitNumber(0)
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
