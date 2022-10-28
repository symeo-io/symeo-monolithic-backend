package io.symeo.monolithic.backend.domain.bff.service;

import io.symeo.monolithic.backend.domain.bff.model.metric.AverageCycleTime;
import io.symeo.monolithic.backend.domain.bff.model.metric.AverageCycleTimeFactory;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTimePieceFactory;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.bff.service.insights.CycleTimeService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CycleTimeServiceTest {

    @Test
    void should_filter_pull_requests_without_commits() {
        // Given
        final AverageCycleTimeFactory averageCycleTimeFactory = mock(AverageCycleTimeFactory.class);
        final CycleTimePieceFactory cycleTimePieceFactory = mock(CycleTimePieceFactory.class);
        final CycleTimeService cycleTimeService = new CycleTimeService(averageCycleTimeFactory, cycleTimePieceFactory);

        // When
        final Optional<AverageCycleTime> emptyAverageCycleTime = cycleTimeService.buildForTagRegexSettings(List.of(
                PullRequestView.builder().commitShaList(List.of()).build(),
                PullRequestView.builder().build()
        ), List.of(), List.of());

        // Then
        assertThat(emptyAverageCycleTime).isEmpty();
    }


}
