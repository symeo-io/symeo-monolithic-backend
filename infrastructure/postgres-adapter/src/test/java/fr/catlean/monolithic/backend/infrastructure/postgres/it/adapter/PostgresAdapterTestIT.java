package fr.catlean.monolithic.backend.infrastructure.postgres.it.adapter;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.helper.DateHelper;
import fr.catlean.monolithic.backend.domain.model.PullRequest;
import fr.catlean.monolithic.backend.domain.model.insight.DataCompareToLimit;
import fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram;
import fr.catlean.monolithic.backend.infrastructure.postgres.PostgresAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.PullRequestEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.PullRequestHistogramDataEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.it.SetupConfiguration;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.PullRequestHistogramRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.PullRequestRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = SetupConfiguration.class)
public class PostgresAdapterTestIT {

    private final Faker faker = new Faker();

    @Autowired
    private PullRequestRepository pullRequestRepository;
    @Autowired
    private PullRequestHistogramRepository pullRequestHistogramRepository;


    @AfterEach
    void tearDown() {
        pullRequestHistogramRepository.deleteAll();
    }

    @Test
    void should_save_pull_requests_to_postgres() {
        // Given
        final PostgresAdapter postgresAdapter = new PostgresAdapter(pullRequestRepository,
                pullRequestHistogramRepository);
        final List<PullRequest> pullRequestsToSave = List.of(
                buildPullRequest(1),
                buildPullRequest(2),
                buildPullRequest(3)
        );

        // When
        postgresAdapter.savePullRequestDetails(pullRequestsToSave);

        // Then
        final List<PullRequestEntity> all = pullRequestRepository.findAll();
        assertThat(all).hasSize(pullRequestsToSave.size());
    }


    @Test
    void should_save_pull_request_histograms_to_postgres() {
        // Given
        final String organization1 = faker.name().firstName() + "-1";
        final String organization2 = faker.name().firstName() + "-2";
        final String organization3 = faker.name().firstName() + "-3";
        final PostgresAdapter postgresAdapter = new PostgresAdapter(pullRequestRepository,
                pullRequestHistogramRepository);
        final List<PullRequestHistogram> pullRequestHistograms = List.of(
                buildPullRequestHistogram(organization1),
                buildPullRequestHistogram(organization2),
                buildPullRequestHistogram(organization3)
        );

        // When
        postgresAdapter.savePullRequestHistograms(pullRequestHistograms);

        // Then
        final List<PullRequestHistogramDataEntity> all = pullRequestHistogramRepository.findAll();
        assertThat(all).hasSize(3 * 5);

    }

    @Test
    void should_read_pull_request_histograms_given_an_organization_a_team_a_type() {
        // Given
        final String organization1 = faker.name().firstName() + "-1";
        final String organization2 = faker.name().firstName() + "-2";
        final String team1 = faker.name().firstName() + "-1";
        final String team2 = faker.name().firstName() + "-2";
        final PostgresAdapter postgresAdapter = new PostgresAdapter(pullRequestRepository,
                pullRequestHistogramRepository);
        final List<PullRequestHistogram> pullRequestHistograms = List.of(
                buildPullRequestHistogramForOrgAndTeam(team1, organization1),
                buildPullRequestHistogramForOrgAndTeam(team2, organization2)
        );

        // When
        postgresAdapter.savePullRequestHistograms(pullRequestHistograms);
        final PullRequestHistogram pullRequestHistogram = postgresAdapter.readPullRequestHistogram(organization1,
                team1, PullRequestHistogram.SIZE_LIMIT);

        // Then
        assertThat(pullRequestHistogram).isNotNull();
        assertThat(pullRequestHistogram.getTeam()).isEqualTo(team1);
        assertThat(pullRequestHistogram.getOrganizationAccount()).isEqualTo(organization1);
        assertThat(pullRequestHistogram.getDataByWeek()).hasSize(5);
    }

    private PullRequestHistogram buildPullRequestHistogram(String organizationName) {
        final String team = faker.name().firstName();
        return buildPullRequestHistogramForOrgAndTeam(team, organizationName);
    }

    private PullRequestHistogram buildPullRequestHistogramForOrgAndTeam(String team, String organizationName) {
        final List<DataCompareToLimit> dataCompareToLimits = new ArrayList<>();
        DateHelper.getWeekStartDateForTheLastWeekNumber(5, TimeZone.getTimeZone(ZoneId.systemDefault()))
                .stream().map(date -> new SimpleDateFormat("dd/MM/yyyy").format(date))
                .forEach(dateAsString -> dataCompareToLimits.add(buildDataCompareToLimit(dateAsString)));


        return PullRequestHistogram.builder()
                .organizationAccount(organizationName)
                .team(team)
                .limit(faker.number().randomDigit())
                .type(PullRequestHistogram.SIZE_LIMIT)
                .dataByWeek(dataCompareToLimits)
                .build();
    }


    private DataCompareToLimit buildDataCompareToLimit(final String dateAsString) {
        return DataCompareToLimit.builder()
                .dateAsString(dateAsString)
                .numberAboveLimit(faker.number().randomDigit())
                .numberBelowLimit(faker.number().randomDigit())
                .build();
    }

    private PullRequest buildPullRequest(int id) {
        return PullRequest.builder()
                .id("fake-platform-name-" + id)
                .number(id)
                .title(faker.name().title())
                .lastUpdateDate(faker.date().past(1, TimeUnit.DAYS))
                .creationDate(faker.date().past(7, TimeUnit.DAYS))
                .mergeDate(new Date())
                .vcsUrl(faker.pokemon().name())
                .deletedLineNumber(faker.number().numberBetween(0, 20000))
                .authorLogin(faker.name().firstName())
                .commitNumber(faker.number().randomDigit())
                .addedLineNumber(faker.number().numberBetween(0, 20000))
                .isDraft(true)
                .isMerged(false)
                .build();
    }
}
