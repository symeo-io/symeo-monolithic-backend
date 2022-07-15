package fr.catlean.monolithic.backend.domain.domain;

import fr.catlean.monolithic.backend.domain.model.PullRequest;
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
        final String open = openPR.getState();
        final String merge1 = mergedPR.getState();
        final String merge2 = mergedPRWithCloseDate.getState();
        final String close = closedPR.getState();

        // Then
        assertThat(open).isEqualTo("open");
        assertThat(merge1).isEqualTo("merge");
        assertThat(merge2).isEqualTo("merge");
        assertThat(close).isEqualTo("close");
    }


}
