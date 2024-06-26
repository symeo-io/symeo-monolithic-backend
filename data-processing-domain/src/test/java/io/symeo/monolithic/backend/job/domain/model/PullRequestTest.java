package io.symeo.monolithic.backend.job.domain.model;

import io.symeo.monolithic.backend.job.domain.model.vcs.PullRequest;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class PullRequestTest {
    @Test
    void should_compute_pull_request_status() {
        // Given
        final PullRequest openPR = PullRequest.builder()
                .creationDate(new Date())
                .number(1)
                .build();
        final PullRequest mergedPR = PullRequest.builder()
                .creationDate(new Date())
                .mergeDate(new Date())
                .number(2)
                .build();
        final PullRequest mergedPRWithCloseDate = PullRequest.builder()
                .creationDate(new Date())
                .mergeDate(new Date())
                .closeDate(new Date())
                .number(3)
                .build();
        final PullRequest closedPR = PullRequest.builder()
                .creationDate(new Date())
                .closeDate(new Date())
                .number(4)
                .build();


        // When
        final String open = openPR.getStatus();
        final String merge1 = mergedPR.getStatus();
        final String merge2 = mergedPRWithCloseDate.getStatus();
        final String close = closedPR.getStatus();

        // Then
        assertThat(open).isEqualTo("open");
        assertThat(merge1).isEqualTo("merge");
        assertThat(merge2).isEqualTo("merge");
        assertThat(close).isEqualTo("close");
    }
}
