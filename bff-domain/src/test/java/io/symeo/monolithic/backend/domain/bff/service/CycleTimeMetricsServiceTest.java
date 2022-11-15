package io.symeo.monolithic.backend.domain.bff.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.metric.*;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.bff.port.out.BffExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.bff.service.insights.CycleTimeMetricsService;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.*;
import static io.symeo.monolithic.backend.domain.helper.MetricsHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CycleTimeMetricsServiceTest {


    private static final Faker faker = new Faker();

    @Test
    void should_get_cycle_time_metrics_given_previous_and_current_cycle_times() throws SymeoException {
        // Given
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final CycleTimeFactory cycleTimeFactory = mock(CycleTimeFactory.class);
        final AverageCycleTimeFactory averageCycleTimeFactory = new AverageCycleTimeFactory(cycleTimeFactory);
        final CycleTimeMetricsService cycleTimeMetricsService = new CycleTimeMetricsService(
                bffExpositionStorageAdapter,
                averageCycleTimeFactory
        );
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final UUID teamId = UUID.randomUUID();
        final Date startDate = stringToDate("2022-01-01");
        final Date endDate = stringToDate("2022-02-01");
        final Date previousStartDate =
                getPreviousStartDateFromStartDateAndEndDate(startDate, endDate, organization.getTimeZone());

        final String pullRequestViewId1 = faker.harryPotter().character() + "-1";
        final String pullRequestViewId2 = faker.harryPotter().character() + "-2";

        final List<PullRequestView> currentPullRequests =
                List.of(
                        PullRequestView.builder().id(pullRequestViewId1).mergeDate(stringToDate("2022-01-03")).head("head-1").build(),
                        PullRequestView.builder().id(pullRequestViewId2).mergeDate(stringToDate("2022-01-05")).head("head-2").build()
                );

        final List<CycleTime> currentCycleTimes =
                List.of(
                        CycleTime.builder()
                                .value(190L)
                                .codingTime(100L)
                                .reviewTime(40L)
                                .timeToDeploy(50L)
                                .deployDate(stringToDate("2022-01-04 10:00:00"))
                                .pullRequestView(currentPullRequests.get(0))
                                .build(),
                        CycleTime.builder()
                                .value(440L)
                                .codingTime(100L)
                                .reviewTime(300L)
                                .timeToDeploy(40L)
                                .deployDate(stringToDate("2022-01-10 10:00:00"))
                                .pullRequestView(currentPullRequests.get(0))
                                .build(),
                        CycleTime.builder()
                                .value(200L)
                                .codingTime(50L)
                                .reviewTime(50L)
                                .timeToDeploy(100L)
                                .deployDate(stringToDate("2022-01-20 10:00:00"))
                                .pullRequestView(currentPullRequests.get(0))
                                .build()
                );

        final List<CycleTime> previousCycleTimes =
                List.of(
                        CycleTime.builder()
                                .value(170L)
                                .codingTime(100L)
                                .reviewTime(40L)
                                .timeToDeploy(30L)
                                .deployDate(stringToDate("2021-12-10 10:00:00"))
                                .pullRequestView(currentPullRequests.get(0))
                                .build(),
                        CycleTime.builder()
                                .value(510L)
                                .codingTime(200L)
                                .reviewTime(300L)
                                .timeToDeploy(10L)
                                .deployDate(stringToDate("2021-12-16 10:00:00"))
                                .pullRequestView(currentPullRequests.get(0))
                                .build(),
                        CycleTime.builder()
                                .value(510L)
                                .codingTime(100L)
                                .reviewTime(400L)
                                .timeToDeploy(10L)
                                .deployDate(stringToDate("2021-12-25 10:00:00"))
                                .pullRequestView(currentPullRequests.get(0))
                                .build()
                );

        // When
        when(bffExpositionStorageAdapter.findCycleTimesForTeamIdBetweenStartDateAndEndDate(teamId, startDate, endDate))
                .thenReturn(
                        currentCycleTimes
                );
        when(bffExpositionStorageAdapter.findCycleTimesForTeamIdBetweenStartDateAndEndDate(teamId, previousStartDate, startDate))
                .thenReturn(
                        previousCycleTimes
                );
        final Optional<CycleTimeMetrics> optionalCycleTimeMetrics =
                cycleTimeMetricsService.computeCycleTimeMetricsForTeamIdFromStartDateToEndDate(organization, teamId, startDate, endDate);

        // Then
        assertThat(optionalCycleTimeMetrics).isPresent();
        assertThat(optionalCycleTimeMetrics).isNotNull();
        assertThat(optionalCycleTimeMetrics.get()).isEqualTo(
                CycleTimeMetrics.builder()
                        .average(276.7f)
                        .averageCodingTime(83.3f)
                        .averageReviewTime(130f)
                        .averageTimeToDeploy(63.3f)
                        .averageTendencyPercentage(getTendencyPercentage(276.7f, 396.7f))
                        .averageCodingTimePercentageTendency(getTendencyPercentage(83.3f, 133.3f))
                        .averageReviewTimePercentageTendency(getTendencyPercentage(130f, 246.7f))
                        .averageTimeToDeployPercentageTendency(getTendencyPercentage(63.3f, 16.7f))
                        .currentStartDate(startDate)
                        .currentEndDate(endDate)
                        .previousStartDate(previousStartDate)
                        .previousEndDate(startDate)
                        .build()
        );
    }

    @Test
    void should_get_cycle_time_metrics_without_previous_cycle_times() throws SymeoException {
        // Given
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final CycleTimeFactory cycleTimeFactory = mock(CycleTimeFactory.class);
        final AverageCycleTimeFactory averageCycleTimeFactory = new AverageCycleTimeFactory(cycleTimeFactory);
        final CycleTimeMetricsService cycleTimeMetricsService = new CycleTimeMetricsService(
                bffExpositionStorageAdapter,
                averageCycleTimeFactory
        );
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final UUID teamId = UUID.randomUUID();
        final Date startDate = stringToDate("2022-01-01");
        final Date endDate = stringToDate("2022-02-01");
        final Date previousStartDate =
                getPreviousStartDateFromStartDateAndEndDate(startDate, endDate, organization.getTimeZone());

        final String pullRequestViewId1 = faker.harryPotter().character() + "-1";
        final String pullRequestViewId2 = faker.harryPotter().character() + "-2";

        final List<PullRequestView> currentPullRequests =
                List.of(
                        PullRequestView.builder().id(pullRequestViewId1).mergeDate(stringToDate("2022-01-03")).head("head-1").build(),
                        PullRequestView.builder().id(pullRequestViewId2).mergeDate(stringToDate("2022-01-05")).head("head-2").build()
                );

        final List<CycleTime> currentCycleTimes =
                List.of(
                        CycleTime.builder()
                                .value(190L)
                                .codingTime(100L)
                                .reviewTime(40L)
                                .timeToDeploy(50L)
                                .deployDate(stringToDate("2022-01-04 10:00:00"))
                                .pullRequestView(currentPullRequests.get(0))
                                .build(),
                        CycleTime.builder()
                                .value(440L)
                                .codingTime(100L)
                                .reviewTime(300L)
                                .timeToDeploy(40L)
                                .deployDate(stringToDate("2022-01-10 10:00:00"))
                                .pullRequestView(currentPullRequests.get(0))
                                .build(),
                        CycleTime.builder()
                                .value(200L)
                                .codingTime(50L)
                                .reviewTime(50L)
                                .timeToDeploy(100L)
                                .deployDate(stringToDate("2022-01-20 10:00:00"))
                                .pullRequestView(currentPullRequests.get(0))
                                .build()
                );

        final List<CycleTime> previousCycleTimes = List.of();

        // When
        when(bffExpositionStorageAdapter.findCycleTimesForTeamIdBetweenStartDateAndEndDate(teamId, startDate, endDate))
                .thenReturn(
                        currentCycleTimes
                );
        when(bffExpositionStorageAdapter.findCycleTimesForTeamIdBetweenStartDateAndEndDate(teamId, previousStartDate, startDate))
                .thenReturn(
                        previousCycleTimes
                );
        final Optional<CycleTimeMetrics> optionalCycleTimeMetrics =
                cycleTimeMetricsService.computeCycleTimeMetricsForTeamIdFromStartDateToEndDate(organization, teamId, startDate, endDate);

        // Then
        assertThat(optionalCycleTimeMetrics).isPresent();
        assertThat(optionalCycleTimeMetrics).isNotNull();
        assertThat(optionalCycleTimeMetrics.get()).isEqualTo(
                CycleTimeMetrics.builder()
                        .average(276.7f)
                        .averageCodingTime(83.3f)
                        .averageReviewTime(130f)
                        .averageTimeToDeploy(63.3f)
                        .averageTendencyPercentage(0f)
                        .averageCodingTimePercentageTendency(0f)
                        .averageReviewTimePercentageTendency(0f)
                        .averageTimeToDeployPercentageTendency(0f)
                        .currentStartDate(startDate)
                        .currentEndDate(endDate)
                        .previousStartDate(previousStartDate)
                        .previousEndDate(startDate)
                        .build()
        );
    }

    @Test
    void should_get_cycle_time_pieces_and_construct_cycle_time_pieces_page() throws SymeoException {
        // Given
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final AverageCycleTimeFactory averageCycleTimeFactory = mock(AverageCycleTimeFactory.class);
        final CycleTimeMetricsService cycleTimeMetricsService = new CycleTimeMetricsService(
                bffExpositionStorageAdapter,
                averageCycleTimeFactory
        );
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final UUID teamId = UUID.randomUUID();
        final Date startDate = stringToDate("2022-01-01");
        final Date endDate = stringToDate("2022-02-01");

        final int pageIndex = faker.number().randomDigit();
        final int pageSize = faker.number().randomDigit();
        final String sortBy = "vcs_repository";
        final String sortDir = "desc";

        final String cycleTimePieceId1 = faker.name().firstName() + "-1";
        final String cycleTimePieceId2 = faker.name().firstName() + "-2";
        final String cycleTimePieceId3 = faker.name().firstName() + "-3";
        final String cycleTimePieceId4 = faker.name().firstName() + "-4";
        final String cycleTimePieceId5 = faker.name().firstName() + "-5";

        final List<CycleTimePiece> cycleTimePiecesForTeamIdBetweenStartDateAndEndDate =
                List.of(
                        CycleTimePiece.builder().id(cycleTimePieceId1).build(),
                        CycleTimePiece.builder().id(cycleTimePieceId2).build(),
                        CycleTimePiece.builder().id(cycleTimePieceId3).build(),
                        CycleTimePiece.builder().id(cycleTimePieceId4).build(),
                        CycleTimePiece.builder().id(cycleTimePieceId5).build()
                );

        final List<CycleTimePiece> cycleTimePiecesForTeamIdBetweenStartDateAndEndDatePaginatedAndSorted =
                List.of(
                        CycleTimePiece.builder().id(cycleTimePieceId1).build(),
                        CycleTimePiece.builder().id(cycleTimePieceId2).build()
                );
        // When
        when(bffExpositionStorageAdapter.findCycleTimePiecesForTeamIdBetweenStartDateAndEndDate(teamId, startDate, endDate))
                .thenReturn(cycleTimePiecesForTeamIdBetweenStartDateAndEndDate);
        when(bffExpositionStorageAdapter.findCycleTimePiecesForTeamIdBetweenStartDateAndEndDatePaginatedAndSorted(
                teamId,
                startDate,
                endDate,
                pageIndex,
                pageSize,
                sortBy,
                sortDir
        )).thenReturn(
                cycleTimePiecesForTeamIdBetweenStartDateAndEndDatePaginatedAndSorted
        );

        final CycleTimePiecePage cycleTimePiecePage =
                cycleTimeMetricsService.computeCycleTimePiecesForTeamIdFromStartDateToEndDate(organization,
                        teamId, startDate, endDate, pageIndex, pageSize, sortBy, sortDir);

        // Then
        assertThat(cycleTimePiecePage.getTotalNumberOfPieces()).isEqualTo(cycleTimePiecesForTeamIdBetweenStartDateAndEndDate.size());
        assertThat(cycleTimePiecePage.getTotalNumberOfPages()).isEqualTo(
                (int) Math.ceil(1.0f * cycleTimePiecesForTeamIdBetweenStartDateAndEndDate.size() / pageSize));
        assertThat(cycleTimePiecePage.getCycleTimePieces()).isEqualTo(cycleTimePiecesForTeamIdBetweenStartDateAndEndDatePaginatedAndSorted);
    }
}
