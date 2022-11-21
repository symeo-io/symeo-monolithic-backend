package io.symeo.monolithic.backend.domain.bff.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTime;
import io.symeo.monolithic.backend.domain.bff.model.metric.curve.CycleTimePieceCurve;
import io.symeo.monolithic.backend.domain.bff.model.metric.curve.CycleTimePieceCurveWithAverage;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.bff.port.out.BffExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.bff.service.insights.CycleTimeCurveService;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CycleTimeCurveServiceTest {
    private static final Faker faker = new Faker();

    @Test
    void should_get_empty_cycle_time_curve_data_for_no_cycle_times_found() throws SymeoException {
        // Given
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final CycleTimeCurveService cycleTimeCurveService = new CycleTimeCurveService(bffExpositionStorageAdapter);

        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final UUID teamId = UUID.randomUUID();
        final Date startDate = stringToDate("2022-01-01");
        final Date endDate = stringToDate("2022-02-01");

        // When
        when(bffExpositionStorageAdapter.findCycleTimesForTeamIdBetweenStartDateAndEndDate(teamId, startDate, endDate))
                .thenReturn(
                        List.of()
                );

        final CycleTimePieceCurveWithAverage cycleTimePieceCurveWithAverage =
                cycleTimeCurveService.computeCycleTimePieceCurveWithAverage(organization, teamId, startDate, endDate);

        // Then
        assertThat(cycleTimePieceCurveWithAverage.getAverageCurve().getData()).isEmpty();
        assertThat(cycleTimePieceCurveWithAverage.getCycleTimePieceCurve().getData()).isEmpty();
    }

    @Test
    void should_get_cycle_time_curve_data() throws SymeoException {
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final CycleTimeCurveService cycleTimeCurveService = new CycleTimeCurveService(bffExpositionStorageAdapter);

        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final UUID teamId = UUID.randomUUID();
        final Date startDate = stringToDate("2022-01-01");
        final Date endDate = stringToDate("2022-01-07");

        final String pullRequestViewId1 = faker.harryPotter().character() + "-1";
        final String pullRequestViewId2 = faker.harryPotter().character() + "-2";

        final List<PullRequestView> currentPullRequests =
                List.of(
                        PullRequestView.builder().id(pullRequestViewId1).mergeDate(stringToDate("2022-01-03")).head("head-1").build(),
                        PullRequestView.builder().id(pullRequestViewId2).mergeDate(stringToDate("2022-01-05")).head("head-2").build(),
                        PullRequestView.builder().id(pullRequestViewId2).mergeDate(null).head("head-3").build()
                );

        final CycleTime cycleTime1 =
                CycleTime.builder()
                        .value(faker.number().randomNumber())
                        .codingTime(faker.number().randomNumber())
                        .reviewTime(faker.number().randomNumber())
                        .timeToDeploy(faker.number().randomNumber())
                        .deployDate(stringToDate("2022-01-04 10:00:00"))
                        .pullRequestView(currentPullRequests.get(0))
                        .build();
        final CycleTime cycleTime2 =
                CycleTime.builder()
                        .value(faker.number().randomNumber())
                        .codingTime(faker.number().randomNumber())
                        .reviewTime(faker.number().randomNumber())
                        .timeToDeploy(faker.number().randomNumber())
                        .deployDate(stringToDate("2022-01-05 15:00:00"))
                        .pullRequestView(currentPullRequests.get(1))
                        .build();
        final CycleTime cycleTime3 =
                CycleTime.builder()
                        .value(faker.number().randomNumber())
                        .codingTime(faker.number().randomNumber())
                        .reviewTime(faker.number().randomNumber())
                        .timeToDeploy(faker.number().randomNumber())
                        .updateDate(stringToDate("2022-01-06 08:00:00"))
                        .deployDate(null)
                        .pullRequestView(currentPullRequests.get(2))
                        .build();


        // When
        when(bffExpositionStorageAdapter.findCycleTimesForTeamIdBetweenStartDateAndEndDate(teamId, startDate, endDate))
                .thenReturn(List.of(
                        cycleTime1,
                        cycleTime2,
                        cycleTime3
                ));
        final CycleTimePieceCurveWithAverage cycleTimePieceCurveWithAverage =
                cycleTimeCurveService.computeCycleTimePieceCurveWithAverage(organization, teamId, startDate, endDate);

        // Then
        assertThat(cycleTimePieceCurveWithAverage.getCycleTimePieceCurve().getData()).isNotEmpty();
        assertThat(cycleTimePieceCurveWithAverage.getCycleTimePieceCurve().getData().size()).isEqualTo(3);
        assertThat(cycleTimePieceCurveWithAverage.getCycleTimePieceCurve().getData().get(0)).isEqualTo(
                CycleTimePieceCurve.CyclePieceCurvePoint.builder()
                        .date("2022-01-04")
                        .value(cycleTime1.getValue())
                        .codingTime(cycleTime1.getCodingTime())
                        .reviewTime(cycleTime1.getReviewTime())
                        .timeToDeploy(cycleTime1.getTimeToDeploy())
                        .label(cycleTime1.getPullRequestView().getHead())
                        .link(cycleTime1.getPullRequestView().getVcsUrl())
                        .build()
        );
        assertThat(cycleTimePieceCurveWithAverage.getCycleTimePieceCurve().getData().get(1)).isEqualTo(
                CycleTimePieceCurve.CyclePieceCurvePoint.builder()
                        .date("2022-01-05")
                        .value(cycleTime2.getValue())
                        .codingTime(cycleTime2.getCodingTime())
                        .reviewTime(cycleTime2.getReviewTime())
                        .timeToDeploy(cycleTime2.getTimeToDeploy())
                        .label(cycleTime2.getPullRequestView().getHead())
                        .link(cycleTime2.getPullRequestView().getVcsUrl())
                        .build()
        );
        assertThat(cycleTimePieceCurveWithAverage.getCycleTimePieceCurve().getData().get(2)).isEqualTo(
                CycleTimePieceCurve.CyclePieceCurvePoint.builder()
                        .date("2022-01-06")
                        .value(cycleTime3.getValue())
                        .codingTime(cycleTime3.getCodingTime())
                        .reviewTime(cycleTime3.getReviewTime())
                        .timeToDeploy(cycleTime3.getTimeToDeploy())
                        .label(cycleTime3.getPullRequestView().getHead())
                        .link(cycleTime3.getPullRequestView().getVcsUrl())
                        .build()
        );
    }
}
