package fr.catlean.monolithic.backend.domain.query;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.TeamGoal;
import fr.catlean.monolithic.backend.domain.model.account.TeamStandard;
import fr.catlean.monolithic.backend.domain.model.insight.curve.PieceCurveWithAverage;
import fr.catlean.monolithic.backend.domain.model.insight.view.PullRequestView;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.port.in.TeamGoalFacadeAdapter;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PieceCurveWithAverageQueryTest {
    private final Faker faker = new Faker();

//    @Test
//    void should_compute_pull_request_pull_request_limit_curve_for_organization_and_team() throws CatleanException {
//        // Given
//        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
//        final TeamGoalFacadeAdapter teamGoalFacadeAdapter = mock(TeamGoalFacadeAdapter.class);
//        final CurveQuery curveQuery = new CurveQuery(expositionStorageAdapter, teamGoalFacadeAdapter);
//        final UUID teamId = UUID.randomUUID();
//        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
//        final String startDateRange1 = "startDateRange1";
//        final String startDateRange2 = "startDateRange2";
//        final String startDateRange3 = "startDateRange3";
//        final List<PullRequestView> pullRequestPullRequestSizeViews = List.of(
//                buildPullRequestPullRequestLimitView(300, startDateRange1, PullRequest.MERGE),
//                buildPullRequestPullRequestLimitView(100, startDateRange1, PullRequest.CLOSE),
//                buildPullRequestPullRequestLimitView(400, startDateRange2, PullRequest.MERGE),
//                buildPullRequestPullRequestLimitView(500, startDateRange3, PullRequest.CLOSE),
//                buildPullRequestPullRequestLimitView(600, startDateRange3, PullRequest.OPEN),
//                buildPullRequestPullRequestLimitView(2000, startDateRange3, PullRequest.OPEN)
//        );
//        final TeamGoal teamGoal = TeamGoal.builder()
//                .value(Integer.toString(faker.number().randomDigit()))
//                .standardCode(TeamStandard.TIME_TO_MERGE)
//                .teamId(teamId)
//                .build();
//
//        // When
//        when(teamGoalFacadeAdapter.getTeamGoalForTeamIdAndTeamStandard(teamId, TeamStandard.buildPullRequestSize()))
//                .thenReturn(teamGoal);
//        when(expositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeam(organization, teamId))
//                .thenReturn(pullRequestPullRequestSizeViews);
//        final PieceCurveWithAverage pieceCurveWithAverage = curveQuery.computePullRequestSizeCurve(organization,
//                teamId);
//
//        // Then
//        assertThat(pieceCurveWithAverage.getAverageCurve().getData()).hasSize(3);
//        assertThat(pieceCurveWithAverage.getPieceCurve().getData()).hasSize(6);
//        assertThat(pieceCurveWithAverage.getLimit()).isEqualTo(Integer.parseInt(teamGoal.getValue()));
//    }

    public static PullRequestView buildPullRequestPullRequestLimitView(final Integer limit,
                                                                       final Date creationDate,
                                                                       final Date mergeDate,
                                                                       final Date closeDate,
                                                                       final String status) {
        return PullRequestView.builder()
                .limit(limit)
                .creationDate(creationDate)
                .closeDate(closeDate)
                .mergeDate(mergeDate)
                .status(status)
                .build();
    }


}
