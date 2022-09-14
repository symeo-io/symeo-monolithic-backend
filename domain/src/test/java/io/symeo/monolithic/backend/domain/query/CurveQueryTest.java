package io.symeo.monolithic.backend.domain.query;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.account.TeamGoal;
import io.symeo.monolithic.backend.domain.model.account.TeamStandard;
import io.symeo.monolithic.backend.domain.model.insight.Metrics;
import io.symeo.monolithic.backend.domain.model.insight.curve.PullRequestPieceCurveWithAverage;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import io.symeo.monolithic.backend.domain.port.in.TeamGoalFacadeAdapter;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import org.junit.jupiter.api.Test;

import javax.swing.text.html.Option;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.getPreviousStartDateFromStartDateAndEndDate;
import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CurveQueryTest {
    private final Faker faker = new Faker();

    @Test
    void should_compute_pull_request_pull_request_size_limit_curve_for_organization_and_team() throws SymeoException {
        // Given
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final TeamGoalFacadeAdapter teamGoalFacadeAdapter = mock(TeamGoalFacadeAdapter.class);
        final CurveQuery curveQuery = new CurveQuery(expositionStorageAdapter, teamGoalFacadeAdapter);
        final UUID teamId = UUID.randomUUID();
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final List<PullRequestView> pullRequestPullRequestSizeViews = List.of(
                buildPullRequestPullRequestLimitView(300, stringToDate("2019-01-01"), null, null, PullRequest.MERGE),
                buildPullRequestPullRequestLimitView(300, stringToDate("2019-01-01"), stringToDate("2019-02-01"),
                        null, PullRequest.MERGE),
                buildPullRequestPullRequestLimitView(100, stringToDate("2019-01-01"), null,
                        stringToDate("2019-02-01"), PullRequest.CLOSE),
                buildPullRequestPullRequestLimitView(400, stringToDate("2019-01-01"), stringToDate("2019-02-01"),
                        null, PullRequest.MERGE),
                buildPullRequestPullRequestLimitView(500, stringToDate("2019-01-01"), null,
                        stringToDate("2019-03-01"), PullRequest.CLOSE),
                buildPullRequestPullRequestLimitView(600, stringToDate("2019-01-01"), null, null, PullRequest.OPEN),
                buildPullRequestPullRequestLimitView(600, stringToDate("2019-01-15"), null, null, PullRequest.OPEN),
                buildPullRequestPullRequestLimitView(2000, stringToDate("2019-01-01"), stringToDate("2019-01-15"),
                        null, PullRequest.OPEN),
                buildPullRequestPullRequestLimitView(2000, stringToDate("2018-01-01"), stringToDate("2018-01-15"),
                        null, PullRequest.OPEN),
                buildPullRequestPullRequestLimitView(2000, stringToDate("2020-01-01"), stringToDate("2020-01-15"),
                        null, PullRequest.OPEN)
        );
        final TeamGoal teamGoal = TeamGoal.builder()
                .value(Integer.toString(faker.number().randomDigit()))
                .standardCode(TeamStandard.PULL_REQUEST_SIZE)
                .teamId(teamId)
                .build();
        final Date startDate = stringToDate("2019-01-01");
        final Date endDate = stringToDate("2019-06-01");

        // When
        when(teamGoalFacadeAdapter.getTeamGoalForTeamIdAndTeamStandard(teamId, TeamStandard.buildPullRequestSize()))
                .thenReturn(teamGoal);
        when(expositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization, teamId, startDate, endDate))
                .thenReturn(pullRequestPullRequestSizeViews);
        final PullRequestPieceCurveWithAverage pullRequestPieceCurveWithAverage =
                curveQuery.computePullRequestSizeCurve(organization,
                        teamId, startDate, endDate);

        // Then
        assertThat(pullRequestPieceCurveWithAverage.getAverageCurve().getData()).hasSize(5);
        assertThat(pullRequestPieceCurveWithAverage.getPullRequestPieceCurve().getData()).hasSize(10);
        assertThat(pullRequestPieceCurveWithAverage.getLimit()).isEqualTo(Integer.parseInt(teamGoal.getValue()));
    }

    @Test
    void should_compute_pull_request_pull_request_time_limit_curve_for_organization_and_team() throws SymeoException {
        // Given
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final TeamGoalFacadeAdapter teamGoalFacadeAdapter = mock(TeamGoalFacadeAdapter.class);
        final CurveQuery curveQuery = new CurveQuery(expositionStorageAdapter, teamGoalFacadeAdapter);
        final UUID teamId = UUID.randomUUID();
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final List<PullRequestView> pullRequestPullRequestSizeViews = List.of(
                buildPullRequestPullRequestLimitView(300, stringToDate("2019-01-01"), null, null, PullRequest.MERGE),
                buildPullRequestPullRequestLimitView(300, stringToDate("2019-01-01"), stringToDate("2019-02-01"),
                        null, PullRequest.MERGE),
                buildPullRequestPullRequestLimitView(100, stringToDate("2019-01-01"), null,
                        stringToDate("2019-02-01"), PullRequest.CLOSE),
                buildPullRequestPullRequestLimitView(400, stringToDate("2019-01-01"), stringToDate("2019-02-01"),
                        null, PullRequest.MERGE),
                buildPullRequestPullRequestLimitView(500, stringToDate("2019-01-01"), null,
                        stringToDate("2019-03-01"), PullRequest.CLOSE),
                buildPullRequestPullRequestLimitView(600, stringToDate("2019-01-01"), null, null, PullRequest.OPEN),
                buildPullRequestPullRequestLimitView(600, stringToDate("2019-01-15"), null, null, PullRequest.OPEN),
                buildPullRequestPullRequestLimitView(2000, stringToDate("2019-01-01"), stringToDate("2019-01-15"),
                        null, PullRequest.OPEN),
                buildPullRequestPullRequestLimitView(2000, stringToDate("2018-01-01"), stringToDate("2018-01-15"),
                        null, PullRequest.OPEN),
                buildPullRequestPullRequestLimitView(2000, stringToDate("2020-01-01"), stringToDate("2020-01-15"),
                        null, PullRequest.OPEN)
        );
        final TeamGoal teamGoal = TeamGoal.builder()
                .value(Integer.toString(faker.number().randomDigit()))
                .standardCode(TeamStandard.TIME_TO_MERGE)
                .teamId(teamId)
                .build();
        final Date startDate = stringToDate("2019-01-01");
        final Date endDate = stringToDate("2019-06-01");

        // When
        when(teamGoalFacadeAdapter.getTeamGoalForTeamIdAndTeamStandard(teamId, TeamStandard.buildTimeToMerge()))
                .thenReturn(teamGoal);
        when(expositionStorageAdapter.readPullRequestsTimeToMergeViewForOrganizationAndTeamBetweenStartDateAndEndDate(organization, teamId, startDate, endDate))
                .thenReturn(pullRequestPullRequestSizeViews);
        final PullRequestPieceCurveWithAverage pullRequestPieceCurveWithAverage =
                curveQuery.computeTimeToMergeCurve(organization,
                        teamId, startDate, endDate);

        // Then
        assertThat(pullRequestPieceCurveWithAverage.getAverageCurve().getData()).hasSize(5);
        assertThat(pullRequestPieceCurveWithAverage.getPullRequestPieceCurve().getData()).hasSize(10);
        assertThat(pullRequestPieceCurveWithAverage.getLimit()).isEqualTo(Integer.parseInt(teamGoal.getValue()));
    }

    @Test
    void should_compute_pull_request_pull_request_size_metrics_for_organization_and_team() throws  SymeoException {
        // Given
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final TeamGoalFacadeAdapter teamGoalFacadeAdapter = mock(TeamGoalFacadeAdapter.class);
        final CurveQuery curveQuery = new CurveQuery(expositionStorageAdapter, teamGoalFacadeAdapter);
        final UUID teamId = UUID.randomUUID();
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final List<PullRequestView> currentPullRequestViews = List.of(
                buildPullRequestPullRequestLimitView(2000, stringToDate("2020-01-01"), stringToDate("2020-01-15"),
                        null, PullRequest.OPEN),
                buildPullRequestPullRequestLimitView(500, stringToDate("2020-01-01"), stringToDate("2020-01-02"),
                        null, PullRequest.MERGE),
                buildPullRequestPullRequestLimitView(300, stringToDate("2020-01-14"), null, null, PullRequest.MERGE),
                buildPullRequestPullRequestLimitView(300, stringToDate("2020-01-22"), null,
                        stringToDate("2020-01-24"), PullRequest.CLOSE),
                buildPullRequestPullRequestLimitView(300, stringToDate("2020-01-25"), stringToDate("2020-01-27"),
                        null, PullRequest.OPEN)
        );

        final List<PullRequestView> previousPullRequestViews = List.of(
                buildPullRequestPullRequestLimitView(2000, stringToDate("2019-12-15"), stringToDate("2019-12-20"),
                        null, PullRequest.OPEN),
                buildPullRequestPullRequestLimitView(200, stringToDate("2019-12-03"), stringToDate("2019-12-05"),
                        null, PullRequest.MERGE),
                buildPullRequestPullRequestLimitView(100, stringToDate("2019-12-10"), null, stringToDate("2019-12-12"), PullRequest.CLOSE),
                buildPullRequestPullRequestLimitView(400, stringToDate("2019-12-14"), stringToDate("2019-12-16"),
                        null, PullRequest.MERGE),
                buildPullRequestPullRequestLimitView(300, stringToDate("2019-12-25"), null, null, PullRequest.MERGE)
        );

        final TeamGoal teamGoal = TeamGoal.builder()
                .value(Integer.toString(faker.number().randomDigit()))
                .standardCode(TeamStandard.PULL_REQUEST_SIZE)
                .teamId(teamId)
                .build();
        final Date startDate = stringToDate("2020-01-01");
        final Date endDate = stringToDate("2020-02-01");

        // When
        when(teamGoalFacadeAdapter.getTeamGoalForTeamIdAndTeamStandard(teamId, TeamStandard.buildPullRequestSize()))
                .thenReturn(teamGoal);
        when(expositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization, teamId, startDate, endDate))
                .thenReturn(currentPullRequestViews);
        when(expositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization, teamId, getPreviousStartDateFromStartDateAndEndDate(startDate, endDate,
                organization.getTimeZone()), startDate))
                .thenReturn(previousPullRequestViews);
        final Metrics metrics = curveQuery.computePullRequestSizeMetrics(organization,
                teamId, startDate, endDate);

        // Then
        assertThat(metrics.getCurrentAverage()).isEqualTo(680);
        assertThat(metrics.getPreviousAverage()).isEqualTo(600);
        assertThat(metrics.getAverageTendency()).isEqualTo(13.3);
        assertThat(metrics.getCurrentStartDate()).isEqualTo(stringToDate("2020-01-01"));
        assertThat(metrics.getCurrentEndDate()).isEqualTo(stringToDate("2020-02-01"));
        assertThat(metrics.getPreviousEndDate()).isEqualTo(stringToDate("2020-01-01"));
        assertThat(metrics.getPreviousStartDate()).isEqualTo(stringToDate("2019-12-01"));
    }
    @Test
    void should_compute_pull_request_pull_request_time_to_merge_metrics_for_organisation_and_team() throws SymeoException {
        // Given
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final TeamGoalFacadeAdapter teamGoalFacadeAdapter = mock(TeamGoalFacadeAdapter.class);
        final CurveQuery curveQuery = new CurveQuery(expositionStorageAdapter, teamGoalFacadeAdapter);
        final UUID teamId = UUID.randomUUID();
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final List<PullRequestView> currentPullRequestViews = List.of(
                buildPullRequestPullRequestLimitView(2000, stringToDate("2020-01-01"), stringToDate("2020-01-15"),
                        null, PullRequest.OPEN),
                buildPullRequestPullRequestLimitView(500, stringToDate("2020-01-01"), stringToDate("2020-01-02"),
                        null, PullRequest.MERGE),
                buildPullRequestPullRequestLimitView(300, stringToDate("2020-01-14"), stringToDate("2020-01-16"),
                        null, PullRequest.MERGE),
                buildPullRequestPullRequestLimitView(300, stringToDate("2020-01-25"), stringToDate("2020-01-27"),
                        null, PullRequest.OPEN)
        );

        final List<PullRequestView> previousPullRequestViews = List.of(
                buildPullRequestPullRequestLimitView(2000, stringToDate("2019-12-15"), stringToDate("2019-12-20"),
                        null, PullRequest.OPEN),
                buildPullRequestPullRequestLimitView(200, stringToDate("2019-12-03"), stringToDate("2019-12-05"),
                        null, PullRequest.MERGE),
                buildPullRequestPullRequestLimitView(400, stringToDate("2019-12-14"), stringToDate("2019-12-16"),
                        null, PullRequest.MERGE),
                buildPullRequestPullRequestLimitView(300, stringToDate("2019-12-25"), stringToDate("2019-12-30"),
                        null, PullRequest.MERGE)
        );

        final TeamGoal teamGoal = TeamGoal.builder()
                .value(Integer.toString(faker.number().randomDigit()))
                .standardCode(TeamStandard.TIME_TO_MERGE)
                .teamId(teamId)
                .build();
        final Date startDate = stringToDate("2020-01-01");
        final Date endDate = stringToDate("2020-02-01");

        // When
        when(teamGoalFacadeAdapter.getTeamGoalForTeamIdAndTeamStandard(teamId, TeamStandard.buildTimeToMerge()))
                .thenReturn(teamGoal);
        when(expositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization, teamId, startDate, endDate))
                .thenReturn(currentPullRequestViews);
        when(expositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization, teamId, getPreviousStartDateFromStartDateAndEndDate(startDate, endDate,
                organization.getTimeZone()), startDate))
                .thenReturn(previousPullRequestViews);
        final Metrics metrics = curveQuery.computePullRequestTimeToMergeMetrics(organization,
                teamId, startDate, endDate);

        // Then
        assertThat(metrics.getCurrentAverage()).isEqualTo(4.8);
        assertThat(metrics.getPreviousAverage()).isEqualTo(3.5);
        assertThat(metrics.getAverageTendency()).isEqualTo(37.1);
        assertThat(metrics.getCurrentStartDate()).isEqualTo(stringToDate("2020-01-01"));
        assertThat(metrics.getCurrentEndDate()).isEqualTo(stringToDate("2020-02-01"));
        assertThat(metrics.getPreviousEndDate()).isEqualTo(stringToDate("2020-01-01"));
        assertThat(metrics.getPreviousStartDate()).isEqualTo(stringToDate("2019-12-01"));
    }

    @Test
    void should_compute_pull_request_pull_request_size_metrics_for_organisation_and_team_without_team_goal() throws SymeoException {
        // Given
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final TeamGoalFacadeAdapter teamGoalFacadeAdapter = mock(TeamGoalFacadeAdapter.class);
        final CurveQuery curveQuery = new CurveQuery(expositionStorageAdapter, teamGoalFacadeAdapter);
        final UUID teamId = UUID.randomUUID();
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final List<PullRequestView> currentPullRequestViews = List.of(
                buildPullRequestPullRequestLimitView(2000, stringToDate("2020-01-01"), stringToDate("2020-01-15"),
                        null, PullRequest.OPEN)
        );

        final List<PullRequestView> previousPullRequestViews = List.of(
                buildPullRequestPullRequestLimitView(300, stringToDate("2019-12-25"), null, null, PullRequest.MERGE)
        );
        final Optional<TeamGoal> optionalTeamGoal = Optional.empty();
        final Date startDate = stringToDate("2020-01-01");
        final Date endDate = stringToDate("2020-02-01");

        // When
        when(teamGoalFacadeAdapter.getOptionalTeamGoalForTeamIdAndTeamStandard(teamId, TeamStandard.buildTimeToMerge()))
                .thenReturn(optionalTeamGoal);
        when(expositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization, teamId, startDate, endDate))
                .thenReturn(currentPullRequestViews);
        when(expositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization, teamId, getPreviousStartDateFromStartDateAndEndDate(startDate, endDate,
                organization.getTimeZone()), startDate))
                .thenReturn(previousPullRequestViews);

        final Metrics metrics = curveQuery.computePullRequestSizeMetrics(organization,
                teamId, startDate, endDate);

        // Then
        assertThat(metrics.getCurrentAverage()).isEqualTo(2000);
        assertThat(metrics.getPreviousAverage()).isEqualTo(300);
        assertThat(metrics.getCurrentStartDate()).isEqualTo(stringToDate("2020-01-01"));
        assertThat(metrics.getCurrentEndDate()).isEqualTo(stringToDate("2020-02-01"));
        assertThat(metrics.getPreviousEndDate()).isEqualTo(stringToDate("2020-01-01"));
        assertThat(metrics.getPreviousStartDate()).isEqualTo(stringToDate("2019-12-01"));
    }@Test
    void should_compute_pull_request_pull_request_time_to_merge_metrics_for_organisation_and_team_without_team_goal() throws SymeoException {
        // Given
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final TeamGoalFacadeAdapter teamGoalFacadeAdapter = mock(TeamGoalFacadeAdapter.class);
        final CurveQuery curveQuery = new CurveQuery(expositionStorageAdapter, teamGoalFacadeAdapter);
        final UUID teamId = UUID.randomUUID();
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final List<PullRequestView> currentPullRequestViews = List.of(
                buildPullRequestPullRequestLimitView(2000, stringToDate("2020-01-01"), stringToDate("2020-01-15"),
                        null, PullRequest.OPEN)
        );

        final List<PullRequestView> previousPullRequestViews = List.of(
                buildPullRequestPullRequestLimitView(300, stringToDate("2019-12-25"), stringToDate("2019-12-30"),
                null, PullRequest.MERGE)
        );

        final Optional<TeamGoal> optionalTeamGoal = Optional.empty();
        final Date startDate = stringToDate("2020-01-01");
        final Date endDate = stringToDate("2020-02-01");

        // When
        when(teamGoalFacadeAdapter.getOptionalTeamGoalForTeamIdAndTeamStandard(teamId, TeamStandard.buildTimeToMerge()))
                .thenReturn(optionalTeamGoal);
        when(expositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization, teamId, startDate, endDate))
                .thenReturn(currentPullRequestViews);
        when(expositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization, teamId, getPreviousStartDateFromStartDateAndEndDate(startDate, endDate,
                organization.getTimeZone()), startDate))
                .thenReturn(previousPullRequestViews);

        final Metrics metrics = curveQuery.computePullRequestTimeToMergeMetrics(organization,
                teamId, startDate, endDate);

        // Then
        assertThat(metrics.getCurrentAverage()).isEqualTo(14);
        assertThat(metrics.getPreviousAverage()).isEqualTo(5);
        assertThat(metrics.getCurrentStartDate()).isEqualTo(stringToDate("2020-01-01"));
        assertThat(metrics.getCurrentEndDate()).isEqualTo(stringToDate("2020-02-01"));
        assertThat(metrics.getPreviousEndDate()).isEqualTo(stringToDate("2020-01-01"));
        assertThat(metrics.getPreviousStartDate()).isEqualTo(stringToDate("2019-12-01"));
    }

    public static PullRequestView buildPullRequestPullRequestLimitView(final Integer limit,
                                                                       final Date creationDate,
                                                                       final Date mergeDate,
                                                                       final Date closeDate,
                                                                       final String status) {
        return PullRequestView.builder()
                .limit(limit.floatValue())
                .addedLineNumber(limit)
                .deletedLineNumber(0)
                .creationDate(creationDate)
                .closeDate(closeDate)
                .mergeDate(mergeDate)
                .status(status)
                .build();
    }


}
