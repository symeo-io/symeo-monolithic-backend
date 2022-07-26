package fr.catlean.monolithic.backend.domain.query;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.insight.Curve;
import fr.catlean.monolithic.backend.domain.model.insight.view.PullRequestTimeToMergeView;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CurveQueryTest {
    private final Faker faker = new Faker();

    @Test
    void should_compute_pull_request_time_to_merge_curve_for_organization_and_team() {
        // Given
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final CurveQuery curveQuery = new CurveQuery(expositionStorageAdapter);
        final String teamName = faker.dragonBall().character();
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final String startDateRange1 = "startDateRange1";
        final String startDateRange2 = "startDateRange2";
        final String startDateRange3 = "startDateRange3";
        // When
        when(expositionStorageAdapter.readPullRequestsTimeToMergeViewForOrganizationAndTeam(organization, teamName))
                .thenReturn(
                        List.of(
                                buildPullRequestTimeToMergeView(3, startDateRange1, PullRequest.MERGE),
                                buildPullRequestTimeToMergeView(1, startDateRange1, PullRequest.CLOSE),
                                buildPullRequestTimeToMergeView(3, startDateRange2, PullRequest.MERGE),
                                buildPullRequestTimeToMergeView(3, startDateRange3, PullRequest.CLOSE),
                                buildPullRequestTimeToMergeView(3, startDateRange3, PullRequest.OPEN),
                                buildPullRequestTimeToMergeView(3, startDateRange3, PullRequest.OPEN)
                        )
                );
        final Curve curve = curveQuery.computeTimeToMergeCurve(organization, teamName);

        // Then
    }


    private PullRequestTimeToMergeView buildPullRequestTimeToMergeView(final Integer daysOpen,
                                                                       final String startDateRange,
                                                                       final String status) {
        return PullRequestTimeToMergeView.builder()
                .daysOpen(daysOpen)
                .startDateRange(startDateRange)
                .status(status)
                .build();
    }
}
