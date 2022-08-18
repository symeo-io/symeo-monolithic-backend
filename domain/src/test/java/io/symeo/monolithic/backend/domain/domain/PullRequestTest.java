package io.symeo.monolithic.backend.domain.domain;

import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class PullRequestTest {

    @Test
    void should_compute_pull_request_status() {
        // Given
        final PullRequest openPR = PullRequest.builder()
                .creationDate(new Date())
                .build();
        final PullRequest mergedPR = PullRequest.builder()
                .creationDate(new Date())
                .mergeDate(new Date())
                .build();
        final PullRequest mergedPRWithCloseDate = PullRequest.builder()
                .creationDate(new Date())
                .mergeDate(new Date())
                .closeDate(new Date())
                .build();
        final PullRequest closedPR = PullRequest.builder()
                .creationDate(new Date())
                .closeDate(new Date())
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
