package io.symeo.monolithic.backend.domain.service;

import io.symeo.monolithic.backend.domain.model.insight.AverageLeadTime;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.service.insights.LeadTimeService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class LeadTimeServiceTest {

    @Test
    void should_filter_pull_requests_without_commits() {
        // Given
        final LeadTimeService leadTimeService = new LeadTimeService();

        // When
        final Optional<AverageLeadTime> emptyAverageLeadTime = leadTimeService.buildForTagRegexSettings(List.of(
                PullRequestView.builder().commitShaList(List.of()).build(),
                PullRequestView.builder().build()
        ), List.of(), List.of());


        // Then
        assertThat(emptyAverageLeadTime).isEmpty();
    }
}
