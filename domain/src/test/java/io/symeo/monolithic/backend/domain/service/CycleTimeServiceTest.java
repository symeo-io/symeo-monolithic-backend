package io.symeo.monolithic.backend.domain.service;

import io.symeo.monolithic.backend.domain.model.insight.AverageCycleTime;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.service.insights.CycleTimeService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class CycleTimeServiceTest {

    @Test
    void should_filter_pull_requests_without_commits() {
        // Given
        final CycleTimeService cycleTimeService = new CycleTimeService();

        // When
        final Optional<AverageCycleTime> emptyAverageCycleTime = cycleTimeService.buildForTagRegexSettings(List.of(
                PullRequestView.builder().commitShaList(List.of()).build(),
                PullRequestView.builder().build()
        ), List.of(), List.of());

        // Then
        assertThat(emptyAverageCycleTime).isEmpty();
    }





}
