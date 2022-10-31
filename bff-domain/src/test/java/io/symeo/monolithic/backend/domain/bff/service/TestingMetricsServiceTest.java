package io.symeo.monolithic.backend.domain.bff.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.Team;
import io.symeo.monolithic.backend.domain.bff.model.metric.TestingMetrics;
import io.symeo.monolithic.backend.domain.bff.model.vcs.RepositoryView;
import io.symeo.monolithic.backend.domain.bff.port.out.TeamStorage;
import io.symeo.monolithic.backend.domain.bff.service.insights.TestingMetricsService;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.testing.CommitTestingData;
import io.symeo.monolithic.backend.job.domain.model.testing.CoverageData;
import io.symeo.monolithic.backend.job.domain.port.in.CommitTestingDataFacadeAdapter;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestingMetricsServiceTest {
    private static final Faker faker = new Faker();

    @Test
    void should_return_empty_metrics_for_not_found_team() throws SymeoException {
        // Given
        final TeamStorage teamStorage = mock(TeamStorage.class);
        final CommitTestingDataFacadeAdapter commitTestingDataFacadeAdapter = mock(CommitTestingDataFacadeAdapter.class);
        final TestingMetricsService testingMetricsService = new TestingMetricsService(teamStorage, commitTestingDataFacadeAdapter);

        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final UUID teamId = UUID.randomUUID();
        final Date startDate = stringToDate("2022-01-01");
        final Date endDate = stringToDate("2022-02-01");

        // When
        when(teamStorage.findById(teamId)).thenReturn(Optional.empty());
        final Optional<TestingMetrics> optionalTestingMetrics =
                testingMetricsService.computeTestingMetricsForTeamIdFromStartDateToEndDate(organization, teamId
                        , startDate, endDate);

        // Then
        assertThat(optionalTestingMetrics).isNotNull();
        assertThat(optionalTestingMetrics).isEmpty();
    }

    @Test
    void should_get_testing_metrics() throws SymeoException {
        // Given
        final TeamStorage teamStorage = mock(TeamStorage.class);
        final CommitTestingDataFacadeAdapter commitTestingDataFacadeAdapter = mock(CommitTestingDataFacadeAdapter.class);
        final TestingMetricsService testingMetricsService = new TestingMetricsService(teamStorage, commitTestingDataFacadeAdapter);

        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final UUID teamId = UUID.randomUUID();
        final Date startDate = stringToDate("2022-01-01");
        final Date endDate = stringToDate("2022-02-01");

        final String repo1Name = faker.starTrek().location();
        final String repo2Name = faker.starTrek().location();
        final String repo1DefaultBranch = faker.starTrek().character();
        final String repo2DefaultBranch = faker.starTrek().character();

        final Team team = Team.builder()
                .id(teamId)
                .repositories(List.of(
                        RepositoryView.builder().name(repo1Name).defaultBranch(repo1DefaultBranch).build(),
                        RepositoryView.builder().name(repo2Name).defaultBranch(repo2DefaultBranch).build()
                ))
                .build();

        // When
        when(teamStorage.findById(teamId)).thenReturn(Optional.of(team));

        when(commitTestingDataFacadeAdapter.getLastTestingDataForRepoAndBranchAndDate(organization.getId(), repo1Name, repo1DefaultBranch, endDate))
                .thenReturn(Optional.of(CommitTestingData.builder()
                        .unitTestCount(10)
                        .integrationTestCount(20)
                        .testLineCount(100)
                        .codeLineCount(300)
                        .coverage(CoverageData.builder()
                                .totalBranchCount(100)
                                .coveredBranches(50).build()
                        )
                        .build()));

        when(commitTestingDataFacadeAdapter.getLastTestingDataForRepoAndBranchAndDate(organization.getId(), repo2Name, repo2DefaultBranch, endDate))
                .thenReturn(Optional.of(CommitTestingData.builder()
                        .unitTestCount(20)
                        .integrationTestCount(30)
                        .testLineCount(200)
                        .codeLineCount(600)
                        .coverage(CoverageData.builder()
                                .totalBranchCount(200)
                                .coveredBranches(100).build()
                        )
                        .build()));

        when(commitTestingDataFacadeAdapter.getLastTestingDataForRepoAndBranchAndDate(organization.getId(), repo1Name, repo1DefaultBranch, startDate))
                .thenReturn(Optional.of(CommitTestingData.builder()
                        .unitTestCount(5)
                        .integrationTestCount(10)
                        .testLineCount(50)
                        .codeLineCount(150)
                        .coverage(CoverageData.builder()
                                .totalBranchCount(100)
                                .coveredBranches(25).build()
                        )
                        .build()));

        when(commitTestingDataFacadeAdapter.getLastTestingDataForRepoAndBranchAndDate(organization.getId(), repo2Name, repo2DefaultBranch, startDate))
                .thenReturn(Optional.of(CommitTestingData.builder()
                        .unitTestCount(10)
                        .integrationTestCount(15)
                        .testLineCount(100)
                        .codeLineCount(300)
                        .coverage(CoverageData.builder()
                                .totalBranchCount(200)
                                .coveredBranches(50).build()
                        )
                        .build()));

        final Optional<TestingMetrics> optionalTestingMetrics =
                testingMetricsService.computeTestingMetricsForTeamIdFromStartDateToEndDate(organization, teamId
                        , startDate, endDate);

        // Then
        assertThat(optionalTestingMetrics).isNotNull();
        assertThat(optionalTestingMetrics).isPresent();
        assertThat(optionalTestingMetrics.get().getTestCount()).isEqualTo(80);
        assertThat(optionalTestingMetrics.get().getTestCountTendencyPercentage()).isEqualTo(100f);
        assertThat(optionalTestingMetrics.get().getTestLineCount()).isEqualTo(300);
        assertThat(optionalTestingMetrics.get().getCodeLineCount()).isEqualTo(900);
        assertThat(optionalTestingMetrics.get().getTestToCodeRatio()).isEqualTo(0.25f);
        assertThat(optionalTestingMetrics.get().getTestToCodeRatioTendencyPercentage()).isEqualTo(0f);
        assertThat(optionalTestingMetrics.get().getCoverage()).isEqualTo(0.5f);
        assertThat(optionalTestingMetrics.get().getCoverageTendencyPercentage()).isEqualTo(100f);
    }
}
