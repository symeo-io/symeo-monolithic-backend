package fr.catlean.monolithic.backend.domain.query;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.insight.curve.PieceCurveWithAverage;
import fr.catlean.monolithic.backend.domain.model.insight.view.PullRequestTimeToMergeView;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PieceCurveWithAverageQueryTest {
    private final Faker faker = new Faker();

    @Test
    void should_compute_pull_request_time_to_merge_curve_for_organization_and_team() throws CatleanException {
        // Given
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final CurveQuery curveQuery = new CurveQuery(expositionStorageAdapter);
        final UUID teamId = UUID.randomUUID();
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final String startDateRange1 = "startDateRange1";
        final String startDateRange2 = "startDateRange2";
        final String startDateRange3 = "startDateRange3";
        final List<PullRequestTimeToMergeView> pullRequestTimeToMergeViews = List.of(
                buildPullRequestTimeToMergeView(3, startDateRange1, PullRequest.MERGE),
                buildPullRequestTimeToMergeView(1, startDateRange1, PullRequest.CLOSE),
                buildPullRequestTimeToMergeView(3, startDateRange2, PullRequest.MERGE),
                buildPullRequestTimeToMergeView(3, startDateRange3, PullRequest.CLOSE),
                buildPullRequestTimeToMergeView(3, startDateRange3, PullRequest.OPEN),
                buildPullRequestTimeToMergeView(3, startDateRange3, PullRequest.OPEN)
        );

        // When
        when(expositionStorageAdapter.readPullRequestsTimeToMergeViewForOrganizationAndTeam(organization, teamId))
                .thenReturn(pullRequestTimeToMergeViews);
        final PieceCurveWithAverage pieceCurveWithAverage = curveQuery.computeTimeToMergeCurve(organization, teamId);

        // Then
        assertThat(pieceCurveWithAverage.getAverageCurve().getData()).hasSize(3);
        assertThat(pieceCurveWithAverage.getPieceCurve().getData()).hasSize(6);
    }

    public static PullRequestTimeToMergeView buildPullRequestTimeToMergeView(final Integer daysOpen,
                                                                             final String startDateRange,
                                                                             final String status) {
        return PullRequestTimeToMergeView.builder()
                .daysOpen(daysOpen)
                .startDateRange(startDateRange)
                .status(status)
                .build();
    }


}
