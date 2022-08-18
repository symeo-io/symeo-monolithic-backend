package io.symeo.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.helper.DateHelper;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.account.Team;
import io.symeo.monolithic.backend.domain.model.account.TeamGoal;
import io.symeo.monolithic.backend.domain.model.account.TeamStandard;
import io.symeo.monolithic.backend.domain.model.insight.PullRequestHistogram;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import io.symeo.monolithic.backend.domain.service.insights.PullRequestHistogramService;
import io.symeo.monolithic.backend.domain.query.PieceCurveWithAverageQueryTest;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.assertj.core.api.Assertions.assertThat;

public class PullRequestHistogramServiceTest {

    private final Faker faker = Faker.instance();
    private final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    @Test
    void should_compute_and_save_pull_request_size_histogram_given_simple_pull_request_cases() throws SymeoException {
        // Given
        final PullRequestHistogramService pullRequestHistogramService =
                new PullRequestHistogramService();
        final String repositoryName1 = faker.pokemon().name() + "-1";
        final Team team = Team.builder()
                .id(UUID.randomUUID())
                .name("team1")
                .repositories(List.of(Repository.builder().name(repositoryName1).build()))
                .build();
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .name("fake-orga-account")
                .vcsOrganization(
                        VcsOrganization.builder().name("fake-orga-name").build()
                )
                .teams(List.of(team))
                .build();
        final TeamGoal teamGoal = TeamGoal.fromTeamStandardAndTeamId(TeamStandard.buildPullRequestSize(),
                team.getId(), 500);

        final List<PullRequestView> pullRequestViews = List.of(
                PieceCurveWithAverageQueryTest.buildPullRequestPullRequestLimitView(2000, stringToDate("2022-01-01"), null, null, PullRequest.OPEN),
                PieceCurveWithAverageQueryTest.buildPullRequestPullRequestLimitView(400, stringToDate("2021-01-01"), null, null, PullRequest.OPEN),
                PieceCurveWithAverageQueryTest.buildPullRequestPullRequestLimitView(200, stringToDate("2022-05-01"), null, null, PullRequest.OPEN),
                PieceCurveWithAverageQueryTest.buildPullRequestPullRequestLimitView(1000, stringToDate("2022-01-01"), null, null, PullRequest.OPEN),
                PieceCurveWithAverageQueryTest.buildPullRequestPullRequestLimitView(1000, stringToDate("2022-01-01"), stringToDate("2022-01-15"),
                        null, PullRequest.MERGE),
                PieceCurveWithAverageQueryTest.buildPullRequestPullRequestLimitView(2000, stringToDate("2022-01-11"), stringToDate("2022-03-01"),
                        null, PullRequest.MERGE),
                PieceCurveWithAverageQueryTest.buildPullRequestPullRequestLimitView(100, stringToDate("2022-01-21"), stringToDate("2022-01-30"),
                        null, PullRequest.MERGE),
                PieceCurveWithAverageQueryTest.buildPullRequestPullRequestLimitView(100, stringToDate("2022-01-21"), stringToDate("2022-04-01"),
                        null, PullRequest.MERGE)
        );

        final PullRequestHistogram pullRequestHistogram =
                PullRequestHistogram.builder().
                        type(PullRequestHistogram.SIZE_LIMIT)
                        .limit(teamGoal.getValueAsInteger())
                        .organizationId(organization.getId())
                        .build();
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(1, 3, "2022-01-01");
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(1, 3, "2022-01-08");
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(1, 3, "2022-01-15");
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(3, 3, "2022-01-22");
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(3, 3, "2022-01-29");
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(2, 3, "2022-02-01");

        final int range = 7;
        final List<Date> rangeDates =
                DateHelper.getRangeDatesBetweenStartDateAndEndDateForRange(
                        stringToDate("2022-01-01"),
                        stringToDate("2022-02-01"),
                        range,
                        organization.getTimeZone()
                );


        // When
        PullRequestHistogram pullRequestHistogramResult =
                pullRequestHistogramService.getPullRequestHistogram(PullRequestHistogram.SIZE_LIMIT,
                        pullRequestViews, organization, teamGoal, rangeDates, range);

        // Then
        assertThat(pullRequestHistogramResult).isEqualTo(pullRequestHistogram);
    }

    @Test
    void should_compute_and_save_pull_request_time_histogram_given_simple_pull_request_cases() throws SymeoException {
        // Given
        final PullRequestHistogramService pullRequestHistogramService =
                new PullRequestHistogramService();
        final String repositoryName1 = faker.pokemon().name() + "-1";
        final Team team = Team.builder()
                .id(UUID.randomUUID())
                .name("team1")
                .repositories(List.of(Repository.builder().name(repositoryName1).build()))
                .build();
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .name("fake-orga-account")
                .vcsOrganization(
                        VcsOrganization.builder().name("fake-orga-name").build()
                )
                .teams(List.of(team))
                .build();
        final TeamGoal teamGoal = TeamGoal.fromTeamStandardAndTeamId(TeamStandard.buildTimeToMerge(),
                team.getId(), 5);

        final List<PullRequestView> pullRequestViews = List.of(
                PieceCurveWithAverageQueryTest.buildPullRequestPullRequestLimitView(0, stringToDate("2021-01-01"), null, null, PullRequest.OPEN),
                PieceCurveWithAverageQueryTest.buildPullRequestPullRequestLimitView(0, stringToDate("2022-01-01"), null, null, PullRequest.OPEN),
                PieceCurveWithAverageQueryTest.buildPullRequestPullRequestLimitView(0, stringToDate("2022-01-01"), null, null, PullRequest.OPEN),
                PieceCurveWithAverageQueryTest.buildPullRequestPullRequestLimitView(0, stringToDate("2022-01-01"), stringToDate("2022-01-15"),
                        null, PullRequest.MERGE),
                PieceCurveWithAverageQueryTest.buildPullRequestPullRequestLimitView(0, stringToDate("2022-05-01"), null, null, PullRequest.OPEN),
                PieceCurveWithAverageQueryTest.buildPullRequestPullRequestLimitView(0, stringToDate("2022-01-11"), stringToDate("2022-03-01"),
                        null, PullRequest.MERGE),
                PieceCurveWithAverageQueryTest.buildPullRequestPullRequestLimitView(0, stringToDate("2022-01-21"), stringToDate("2022-01-30"),
                        null, PullRequest.MERGE),
                PieceCurveWithAverageQueryTest.buildPullRequestPullRequestLimitView(0, stringToDate("2022-01-21"), stringToDate("2022-04-01"),
                        null, PullRequest.MERGE)
        );

        final PullRequestHistogram pullRequestHistogram =
                PullRequestHistogram.builder().
                        type(PullRequestHistogram.TIME_LIMIT)
                        .limit(teamGoal.getValueAsInteger())
                        .organizationId(organization.getId())
                        .build();
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(3, 1, "2022-01-01");
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(0, 4, "2022-01-08");
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(1, 3, "2022-01-15");
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(2, 4, "2022-01-22");
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(0, 6, "2022-01-29");
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(0, 5, "2022-02-01");

        final int range = 7;
        final List<Date> rangeDates =
                DateHelper.getRangeDatesBetweenStartDateAndEndDateForRange(
                        stringToDate("2022-01-01"),
                        stringToDate("2022-02-01"),
                        range,
                        organization.getTimeZone()
                );


        // When
        PullRequestHistogram pullRequestHistogramResult =
                pullRequestHistogramService.getPullRequestHistogram(PullRequestHistogram.TIME_LIMIT,
                        pullRequestViews, organization, teamGoal, rangeDates, range);

        // Then
        assertThat(pullRequestHistogramResult).isEqualTo(pullRequestHistogram);
    }
}
