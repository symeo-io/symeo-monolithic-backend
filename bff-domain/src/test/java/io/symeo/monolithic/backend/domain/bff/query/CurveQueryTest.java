package io.symeo.monolithic.backend.domain.bff.query;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.TeamGoal;
import io.symeo.monolithic.backend.domain.bff.model.account.TeamStandard;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.DeliverySettings;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.DeployDetectionSettings;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.bff.model.metric.Metrics;
import io.symeo.monolithic.backend.domain.bff.model.metric.curve.PullRequestPieceCurveWithAverage;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.bff.port.in.TeamGoalFacadeAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.BffExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.OrganizationStorageAdapter;
import io.symeo.monolithic.backend.domain.bff.query.CurveQuery;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final TeamGoalFacadeAdapter teamGoalFacadeAdapter = mock(TeamGoalFacadeAdapter.class);
        final OrganizationStorageAdapter organizationStorageAdapter = mock(OrganizationStorageAdapter.class);
        final CurveQuery curveQuery = new CurveQuery(bffExpositionStorageAdapter, teamGoalFacadeAdapter, organizationStorageAdapter);
        final UUID teamId = UUID.randomUUID();
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final List<PullRequestView> pullRequestPullRequestSizeViews = List.of(
                buildPullRequestPullRequestLimitView(300, stringToDate("2019-01-01"), null, null,
                        PullRequestView.MERGE, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(300, stringToDate("2019-01-01"), stringToDate("2019-02-01"),
                        null, PullRequestView.MERGE, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(100, stringToDate("2019-01-01"), null,
                        stringToDate("2019-02-01"), PullRequestView.CLOSE, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(400, stringToDate("2019-01-01"), stringToDate("2019-02-01"),
                        null, PullRequestView.MERGE, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(500, stringToDate("2019-01-01"), null,
                        stringToDate("2019-03-01"), PullRequestView.CLOSE, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(600, stringToDate("2019-01-01"), null, null, PullRequestView.OPEN, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(600, stringToDate("2019-01-15"), null, null, PullRequestView.OPEN, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(2000, stringToDate("2019-01-01"), stringToDate("2019-01-15"),
                        null, PullRequestView.OPEN, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(2000, stringToDate("2018-01-01"), stringToDate("2018-01-15"),
                        null, PullRequestView.OPEN, "main"),
                buildPullRequestPullRequestLimitView(2000, stringToDate("2020-01-01"), stringToDate("2020-01-15"),
                        null, PullRequestView.OPEN, "main")
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
        when(bffExpositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization, teamId, startDate, endDate))
                .thenReturn(pullRequestPullRequestSizeViews);
        when(organizationStorageAdapter.findOrganizationSettingsForOrganizationId(organization.getId()))
                .thenReturn(Optional.of(OrganizationSettings.builder()
                        .organizationId(organization.getId())
                        .deliverySettings(DeliverySettings.builder()
                                .deployDetectionSettings(DeployDetectionSettings.builder()
                                        .excludeBranchRegexes(List.of("^staging$", "^main$"))
                                        .build())
                                .build())
                        .build()
                ));
        final PullRequestPieceCurveWithAverage pullRequestPieceCurveWithAverage =
                curveQuery.computePullRequestSizeCurve(organization,
                        teamId, startDate, endDate);

        // Then
        assertThat(pullRequestPieceCurveWithAverage.getAverageCurve().getData()).hasSize(4);
        assertThat(pullRequestPieceCurveWithAverage.getPullRequestPieceCurve().getData()).hasSize(8);
        assertThat(pullRequestPieceCurveWithAverage.getLimit()).isEqualTo(Integer.parseInt(teamGoal.getValue()));
    }

    @Test
    void should_compute_pull_request_pull_request_time_limit_curve_for_organization_and_team() throws SymeoException {
        // Given
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final TeamGoalFacadeAdapter teamGoalFacadeAdapter = mock(TeamGoalFacadeAdapter.class);
        final OrganizationStorageAdapter organizationStorageAdapter = mock(OrganizationStorageAdapter.class);
        final CurveQuery curveQuery = new CurveQuery(bffExpositionStorageAdapter, teamGoalFacadeAdapter, organizationStorageAdapter);
        final UUID teamId = UUID.randomUUID();
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final List<PullRequestView> pullRequestPullRequestSizeViews = List.of(
                buildPullRequestPullRequestLimitView(300, stringToDate("2019-01-01"), null, null,
                        PullRequestView.MERGE, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(300, stringToDate("2019-01-01"), stringToDate("2019-02-01"),
                        null, PullRequestView.MERGE, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(100, stringToDate("2019-01-01"), null,
                        stringToDate("2019-02-01"), PullRequestView.CLOSE, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(400, stringToDate("2019-01-01"), stringToDate("2019-02-01"),
                        null, PullRequestView.MERGE, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(500, stringToDate("2019-01-01"), null,
                        stringToDate("2019-03-01"), PullRequestView.CLOSE, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(600, stringToDate("2019-01-01"), null, null, PullRequestView.OPEN, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(600, stringToDate("2019-01-15"), null, null, PullRequestView.OPEN, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(2000, stringToDate("2019-01-01"), stringToDate("2019-01-15"),
                        null, PullRequestView.OPEN, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(2000, stringToDate("2018-01-01"), stringToDate("2018-01-15"),
                        null, PullRequestView.OPEN, "main"),
                buildPullRequestPullRequestLimitView(2000, stringToDate("2020-01-01"), stringToDate("2020-01-15"),
                        null, PullRequestView.OPEN, "main")
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
        when(bffExpositionStorageAdapter.readPullRequestsTimeToMergeViewForOrganizationAndTeamBetweenStartDateAndEndDate(organization, teamId, startDate, endDate))
                .thenReturn(pullRequestPullRequestSizeViews);
        when(organizationStorageAdapter.findOrganizationSettingsForOrganizationId(organization.getId()))
                .thenReturn(Optional.of(OrganizationSettings.builder()
                        .organizationId(organization.getId())
                        .deliverySettings(DeliverySettings.builder()
                                .deployDetectionSettings(DeployDetectionSettings.builder()
                                        .excludeBranchRegexes(List.of("^staging$", "^main$"))
                                        .build())
                                .build())
                        .build()
                ));
        final PullRequestPieceCurveWithAverage pullRequestPieceCurveWithAverage =
                curveQuery.computeTimeToMergeCurve(organization,
                        teamId, startDate, endDate);

        // Then
        assertThat(pullRequestPieceCurveWithAverage.getAverageCurve().getData()).hasSize(4);
        assertThat(pullRequestPieceCurveWithAverage.getPullRequestPieceCurve().getData()).hasSize(8);
        assertThat(pullRequestPieceCurveWithAverage.getLimit()).isEqualTo(Integer.parseInt(teamGoal.getValue()));

    }

    @Test
    void should_compute_pull_request_pull_request_size_metrics_for_organization_and_team() throws SymeoException {
        // Given
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final TeamGoalFacadeAdapter teamGoalFacadeAdapter = mock(TeamGoalFacadeAdapter.class);
        final OrganizationStorageAdapter organizationStorageAdapter = mock(OrganizationStorageAdapter.class);
        final CurveQuery curveQuery = new CurveQuery(bffExpositionStorageAdapter, teamGoalFacadeAdapter, organizationStorageAdapter);
        final UUID teamId = UUID.randomUUID();
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final List<PullRequestView> currentPullRequestViews = List.of(
                buildPullRequestPullRequestLimitView(2000, stringToDate("2020-01-01"), stringToDate("2020-01-15"),
                        null, PullRequestView.OPEN, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(500, stringToDate("2020-01-01"), stringToDate("2020-01-02"),
                        null, PullRequestView.MERGE, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(300, stringToDate("2020-01-14"), null, null,
                        PullRequestView.MERGE, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(300, stringToDate("2020-01-22"), null,
                        stringToDate("2020-01-24"), PullRequestView.CLOSE, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(300, stringToDate("2020-01-25"), stringToDate("2020-01-27"),
                        null, PullRequestView.OPEN, "main")
        );

        final List<PullRequestView> previousPullRequestViews = List.of(
                buildPullRequestPullRequestLimitView(2000, stringToDate("2019-12-15"), stringToDate("2019-12-20"),
                        null, PullRequestView.OPEN, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(200, stringToDate("2019-12-03"), stringToDate("2019-12-05"),
                        null, PullRequestView.MERGE, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(100, stringToDate("2019-12-10"), null,
                        stringToDate("2019-12-12"), PullRequestView.CLOSE, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(400, stringToDate("2019-12-14"), stringToDate("2019-12-16"),
                        null, PullRequestView.MERGE, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(300, stringToDate("2019-12-25"), null,
                        null, PullRequestView.MERGE, "main")
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
        when(bffExpositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization, teamId, startDate, endDate))
                .thenReturn(currentPullRequestViews);
        when(bffExpositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization, teamId, getPreviousStartDateFromStartDateAndEndDate(startDate, endDate,
                organization.getTimeZone()), startDate))
                .thenReturn(previousPullRequestViews);
        when(organizationStorageAdapter.findOrganizationSettingsForOrganizationId(organization.getId()))
                .thenReturn(Optional.of(OrganizationSettings.builder()
                        .organizationId(organization.getId())
                        .deliverySettings(DeliverySettings.builder()
                                .deployDetectionSettings(DeployDetectionSettings.builder()
                                        .excludeBranchRegexes(List.of("^staging$", "^main$"))
                                        .build())
                                .build())
                        .build()
                ));
        final Metrics metrics = curveQuery.computePullRequestSizeMetrics(organization,
                teamId, startDate, endDate);

        // Then
        assertThat(metrics.getCurrentAverage()).isEqualTo(775);
        assertThat(metrics.getPreviousAverage()).isEqualTo(675);
        assertThat(metrics.getAverageTendency()).isEqualTo(14.8);
        assertThat(metrics.getCurrentStartDate()).isEqualTo(stringToDate("2020-01-01"));
        assertThat(metrics.getCurrentEndDate()).isEqualTo(stringToDate("2020-02-01"));
        assertThat(metrics.getPreviousEndDate()).isEqualTo(stringToDate("2020-01-01"));
        assertThat(metrics.getPreviousStartDate()).isEqualTo(stringToDate("2019-12-01"));
    }

    @Test
    void should_compute_pull_request_pull_request_time_to_merge_metrics_for_organisation_and_team() throws SymeoException {
        // Given
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final TeamGoalFacadeAdapter teamGoalFacadeAdapter = mock(TeamGoalFacadeAdapter.class);
        final OrganizationStorageAdapter organizationStorageAdapter = mock(OrganizationStorageAdapter.class);
        final CurveQuery curveQuery = new CurveQuery(bffExpositionStorageAdapter, teamGoalFacadeAdapter, organizationStorageAdapter);
        final UUID teamId = UUID.randomUUID();
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final List<PullRequestView> currentPullRequestViews = List.of(
                buildPullRequestPullRequestLimitView(2000, stringToDate("2020-01-01"), stringToDate("2020-01-15"),
                        null, PullRequestView.OPEN, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(500, stringToDate("2020-01-01"), stringToDate("2020-01-02"),
                        null, PullRequestView.MERGE, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(300, stringToDate("2020-01-14"), stringToDate("2020-01-16"),
                        null, PullRequestView.MERGE, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(300, stringToDate("2020-01-25"), stringToDate("2020-01-27"),
                        null, PullRequestView.OPEN, "main")
        );

        final List<PullRequestView> previousPullRequestViews = List.of(
                buildPullRequestPullRequestLimitView(2000, stringToDate("2019-12-15"), stringToDate("2019-12-20"),
                        null, PullRequestView.OPEN, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(200, stringToDate("2019-12-03"), stringToDate("2019-12-05"),
                        null, PullRequestView.MERGE, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(400, stringToDate("2019-12-14"), stringToDate("2019-12-16"),
                        null, PullRequestView.MERGE, faker.pokemon().name()),
                buildPullRequestPullRequestLimitView(300, stringToDate("2019-12-25"), stringToDate("2019-12-30"),
                        null, PullRequestView.MERGE, "main")
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
        when(bffExpositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization, teamId, startDate, endDate))
                .thenReturn(currentPullRequestViews);
        when(bffExpositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization, teamId, getPreviousStartDateFromStartDateAndEndDate(startDate, endDate,
                organization.getTimeZone()), startDate))
                .thenReturn(previousPullRequestViews);
        when(organizationStorageAdapter.findOrganizationSettingsForOrganizationId(organization.getId()))
                .thenReturn(Optional.of(OrganizationSettings.builder()
                        .organizationId(organization.getId())
                        .deliverySettings(DeliverySettings.builder()
                                .deployDetectionSettings(DeployDetectionSettings.builder()
                                        .excludeBranchRegexes(List.of("^staging$", "^main$"))
                                        .build())
                                .build())
                        .build()
                ));

        final Metrics metrics = curveQuery.computePullRequestTimeToMergeMetrics(organization,
                teamId, startDate, endDate);

        // Then
        assertThat(metrics.getCurrentAverage()).isEqualTo(5.7);
        assertThat(metrics.getPreviousAverage()).isEqualTo(3.0);
        assertThat(metrics.getAverageTendency()).isEqualTo(90.0);
        assertThat(metrics.getCurrentStartDate()).isEqualTo(stringToDate("2020-01-01"));
        assertThat(metrics.getCurrentEndDate()).isEqualTo(stringToDate("2020-02-01"));
        assertThat(metrics.getPreviousEndDate()).isEqualTo(stringToDate("2020-01-01"));
        assertThat(metrics.getPreviousStartDate()).isEqualTo(stringToDate("2019-12-01"));
    }

    @Test
    void should_compute_pull_request_pull_request_size_metrics_for_organisation_and_team_without_team_goal() throws SymeoException {
        // Given
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final TeamGoalFacadeAdapter teamGoalFacadeAdapter = mock(TeamGoalFacadeAdapter.class);
        final OrganizationStorageAdapter organizationStorageAdapter = mock(OrganizationStorageAdapter.class);
        final CurveQuery curveQuery = new CurveQuery(bffExpositionStorageAdapter, teamGoalFacadeAdapter, organizationStorageAdapter);
        final UUID teamId = UUID.randomUUID();
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final List<PullRequestView> currentPullRequestViews = List.of(
                buildPullRequestPullRequestLimitView(2000, stringToDate("2020-01-01"), stringToDate("2020-01-15"),
                        null, PullRequestView.OPEN, faker.pokemon().name())
        );

        final List<PullRequestView> previousPullRequestViews = List.of(
                buildPullRequestPullRequestLimitView(300, stringToDate("2019-12-25"), null, null, PullRequestView.MERGE, faker.pokemon().name())
        );
        final Optional<TeamGoal> optionalTeamGoal = Optional.empty();
        final Date startDate = stringToDate("2020-01-01");
        final Date endDate = stringToDate("2020-02-01");

        // When
        when(teamGoalFacadeAdapter.getOptionalTeamGoalForTeamIdAndTeamStandard(teamId, TeamStandard.buildTimeToMerge()))
                .thenReturn(optionalTeamGoal);
        when(bffExpositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization, teamId, startDate, endDate))
                .thenReturn(currentPullRequestViews);
        when(bffExpositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization, teamId, getPreviousStartDateFromStartDateAndEndDate(startDate, endDate,
                organization.getTimeZone()), startDate))
                .thenReturn(previousPullRequestViews);
        when(organizationStorageAdapter.findOrganizationSettingsForOrganizationId(organization.getId()))
                .thenReturn(Optional.of(OrganizationSettings.builder()
                        .organizationId(organization.getId())
                        .deliverySettings(DeliverySettings.builder()
                                .deployDetectionSettings(DeployDetectionSettings.builder()
                                        .excludeBranchRegexes(List.of("^staging$", "^main$"))
                                        .build())
                                .build())
                        .build()
                ));


        final Metrics metrics = curveQuery.computePullRequestSizeMetrics(organization,
                teamId, startDate, endDate);

        // Then
        assertThat(metrics.getCurrentAverage()).isEqualTo(2000);
        assertThat(metrics.getPreviousAverage()).isEqualTo(300);
        assertThat(metrics.getCurrentStartDate()).isEqualTo(stringToDate("2020-01-01"));
        assertThat(metrics.getCurrentEndDate()).isEqualTo(stringToDate("2020-02-01"));
        assertThat(metrics.getPreviousEndDate()).isEqualTo(stringToDate("2020-01-01"));
        assertThat(metrics.getPreviousStartDate()).isEqualTo(stringToDate("2019-12-01"));
    }

    @Test
    void should_compute_pull_request_pull_request_time_to_merge_metrics_for_organisation_and_team_without_team_goal() throws SymeoException {
        // Given
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final TeamGoalFacadeAdapter teamGoalFacadeAdapter = mock(TeamGoalFacadeAdapter.class);
        final OrganizationStorageAdapter organizationStorageAdapter = mock(OrganizationStorageAdapter.class);
        final CurveQuery curveQuery = new CurveQuery(bffExpositionStorageAdapter, teamGoalFacadeAdapter, organizationStorageAdapter);
        final UUID teamId = UUID.randomUUID();
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final List<PullRequestView> currentPullRequestViews = List.of(
                buildPullRequestPullRequestLimitView(2000, stringToDate("2020-01-01"), stringToDate("2020-01-15"),
                        null, PullRequestView.OPEN, faker.pokemon().name())
        );

        final List<PullRequestView> previousPullRequestViews = List.of(
                buildPullRequestPullRequestLimitView(300, stringToDate("2019-12-25"), stringToDate("2019-12-30"),
                        null, PullRequestView.MERGE, faker.pokemon().name())
        );

        final Optional<TeamGoal> optionalTeamGoal = Optional.empty();
        final Date startDate = stringToDate("2020-01-01");
        final Date endDate = stringToDate("2020-02-01");

        // When
        when(teamGoalFacadeAdapter.getOptionalTeamGoalForTeamIdAndTeamStandard(teamId, TeamStandard.buildTimeToMerge()))
                .thenReturn(optionalTeamGoal);
        when(bffExpositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization, teamId, startDate, endDate))
                .thenReturn(currentPullRequestViews);
        when(bffExpositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization, teamId, getPreviousStartDateFromStartDateAndEndDate(startDate, endDate,
                organization.getTimeZone()), startDate))
                .thenReturn(previousPullRequestViews);
        when(organizationStorageAdapter.findOrganizationSettingsForOrganizationId(organization.getId()))
                .thenReturn(Optional.of(OrganizationSettings.builder()
                        .organizationId(organization.getId())
                        .deliverySettings(DeliverySettings.builder()
                                .deployDetectionSettings(DeployDetectionSettings.builder()
                                        .excludeBranchRegexes(List.of("^staging$", "^main$"))
                                        .build())
                                .build())
                        .build()
                ));

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
                                                                       final String status,
                                                                       final String head) {
        return PullRequestView.builder()
                .limit(limit.floatValue())
                .addedLineNumber(limit)
                .deletedLineNumber(0)
                .creationDate(creationDate)
                .closeDate(closeDate)
                .mergeDate(mergeDate)
                .status(status)
                .head(head)
                .build();
    }


}
