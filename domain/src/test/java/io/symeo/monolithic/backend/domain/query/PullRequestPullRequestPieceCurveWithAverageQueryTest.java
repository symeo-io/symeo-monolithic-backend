package io.symeo.monolithic.backend.domain.query;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.account.TeamGoal;
import io.symeo.monolithic.backend.domain.model.account.TeamStandard;
import io.symeo.monolithic.backend.domain.model.insight.curve.PullRequestPieceCurveWithAverage;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import io.symeo.monolithic.backend.domain.port.in.TeamGoalFacadeAdapter;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PullRequestPullRequestPieceCurveWithAverageQueryTest {
    private final Faker faker = new Faker();

    @Test
    void should_compute_pull_request_pull_request_size_limit_curve_for_organization_and_team() throws SymeoException {
        // Given
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final TeamGoalFacadeAdapter teamGoalFacadeAdapter = mock(TeamGoalFacadeAdapter.class);
        final CurveQuery curveQuery = new CurveQuery(expositionStorageAdapter, teamGoalFacadeAdapter);
        final UUID teamId = UUID.randomUUID();
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final List<PullRequestView> pullRequestPullRequestSizeViews = List.of(
                buildPullRequestPullRequestLimitView(300, stringToDate("2019-01-01"), null, null, PullRequest.MERGE),
                buildPullRequestPullRequestLimitView(300, stringToDate("2019-01-01"), stringToDate("2019-02-01"),
                        null, PullRequest.MERGE),
                buildPullRequestPullRequestLimitView(100, stringToDate("2019-01-01"), null,
                        stringToDate("2019-02-01"), PullRequest.CLOSE),
                buildPullRequestPullRequestLimitView(400, stringToDate("2019-01-01"), stringToDate("2019-02-01"),
                        null, PullRequest.MERGE),
                buildPullRequestPullRequestLimitView(500, stringToDate("2019-01-01"), null,
                        stringToDate("2019-03-01"), PullRequest.CLOSE),
                buildPullRequestPullRequestLimitView(600, stringToDate("2019-01-01"), null, null, PullRequest.OPEN),
                buildPullRequestPullRequestLimitView(600, stringToDate("2019-01-15"), null, null, PullRequest.OPEN),
                buildPullRequestPullRequestLimitView(2000, stringToDate("2019-01-01"), stringToDate("2019-01-15"),
                        null, PullRequest.OPEN),
                buildPullRequestPullRequestLimitView(2000, stringToDate("2018-01-01"), stringToDate("2018-01-15"),
                        null, PullRequest.OPEN),
                buildPullRequestPullRequestLimitView(2000, stringToDate("2020-01-01"), stringToDate("2020-01-15"),
                        null, PullRequest.OPEN)
        );
        final TeamGoal teamGoal = TeamGoal.builder()
                .value(Integer.toString(faker.number().randomDigit()))
                .standardCode(TeamStandard.PULL_REQUEST_SIZE)
                .teamId(teamId)
                .build();

        // When
        when(teamGoalFacadeAdapter.getTeamGoalForTeamIdAndTeamStandard(teamId, TeamStandard.buildPullRequestSize()))
                .thenReturn(teamGoal);
        when(expositionStorageAdapter.readPullRequestsSizeViewForOrganizationAndTeam(organization, teamId))
                .thenReturn(pullRequestPullRequestSizeViews);
        final PullRequestPieceCurveWithAverage pullRequestPieceCurveWithAverage = curveQuery.computePullRequestSizeCurve(organization,
                teamId, stringToDate("2019-01-01"), stringToDate("2019-06-01"));

        // Then
        assertThat(pullRequestPieceCurveWithAverage.getAverageCurve().getData()).hasSize(4);
        assertThat(pullRequestPieceCurveWithAverage.getPullRequestPieceCurve().getData()).hasSize(8);
        assertThat(pullRequestPieceCurveWithAverage.getLimit()).isEqualTo(Integer.parseInt(teamGoal.getValue()));
    }

    @Test
    void should_compute_pull_request_pull_request_time_limit_curve_for_organization_and_team() throws SymeoException {
        // Given
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final TeamGoalFacadeAdapter teamGoalFacadeAdapter = mock(TeamGoalFacadeAdapter.class);
        final CurveQuery curveQuery = new CurveQuery(expositionStorageAdapter, teamGoalFacadeAdapter);
        final UUID teamId = UUID.randomUUID();
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final List<PullRequestView> pullRequestPullRequestSizeViews = List.of(
                buildPullRequestPullRequestLimitView(300, stringToDate("2019-01-01"), null, null, PullRequest.MERGE),
                buildPullRequestPullRequestLimitView(300, stringToDate("2019-01-01"), stringToDate("2019-02-01"),
                        null, PullRequest.MERGE),
                buildPullRequestPullRequestLimitView(100, stringToDate("2019-01-01"), null,
                        stringToDate("2019-02-01"), PullRequest.CLOSE),
                buildPullRequestPullRequestLimitView(400, stringToDate("2019-01-01"), stringToDate("2019-02-01"),
                        null, PullRequest.MERGE),
                buildPullRequestPullRequestLimitView(500, stringToDate("2019-01-01"), null,
                        stringToDate("2019-03-01"), PullRequest.CLOSE),
                buildPullRequestPullRequestLimitView(600, stringToDate("2019-01-01"), null, null, PullRequest.OPEN),
                buildPullRequestPullRequestLimitView(600, stringToDate("2019-01-15"), null, null, PullRequest.OPEN),
                buildPullRequestPullRequestLimitView(2000, stringToDate("2019-01-01"), stringToDate("2019-01-15"),
                        null, PullRequest.OPEN),
                buildPullRequestPullRequestLimitView(2000, stringToDate("2018-01-01"), stringToDate("2018-01-15"),
                        null, PullRequest.OPEN),
                buildPullRequestPullRequestLimitView(2000, stringToDate("2020-01-01"), stringToDate("2020-01-15"),
                        null, PullRequest.OPEN)
        );
        final TeamGoal teamGoal = TeamGoal.builder()
                .value(Integer.toString(faker.number().randomDigit()))
                .standardCode(TeamStandard.TIME_TO_MERGE)
                .teamId(teamId)
                .build();

        // When
        when(teamGoalFacadeAdapter.getTeamGoalForTeamIdAndTeamStandard(teamId, TeamStandard.buildTimeToMerge()))
                .thenReturn(teamGoal);
        when(expositionStorageAdapter.readPullRequestsTimeToMergeViewForOrganizationAndTeam(organization, teamId))
                .thenReturn(pullRequestPullRequestSizeViews);
        final PullRequestPieceCurveWithAverage pullRequestPieceCurveWithAverage = curveQuery.computeTimeToMergeCurve(organization,
                teamId, stringToDate("2019-01-01"), stringToDate("2019-06-01"));

        // Then
        assertThat(pullRequestPieceCurveWithAverage.getAverageCurve().getData()).hasSize(4);
        assertThat(pullRequestPieceCurveWithAverage.getPullRequestPieceCurve().getData()).hasSize(8);
        assertThat(pullRequestPieceCurveWithAverage.getLimit()).isEqualTo(Integer.parseInt(teamGoal.getValue()));
    }


    public static PullRequestView buildPullRequestPullRequestLimitView(final Integer limit,
                                                                       final Date creationDate,
                                                                       final Date mergeDate,
                                                                       final Date closeDate,
                                                                       final String status) {
        return PullRequestView.builder()
                .limit(limit.floatValue())
                .addedLineNumber(limit)
                .deletedLineNumber(0)
                .creationDate(creationDate)
                .closeDate(closeDate)
                .mergeDate(mergeDate)
                .status(status)
                .build();
    }


}
