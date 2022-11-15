package io.symeo.monolithic.backend.domain.bff.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.Team;
import io.symeo.monolithic.backend.domain.bff.model.metric.CommitTestingDataView;
import io.symeo.monolithic.backend.domain.bff.model.metric.TestingMetrics;
import io.symeo.monolithic.backend.domain.bff.model.vcs.RepositoryView;
import io.symeo.monolithic.backend.domain.bff.port.out.BffCommitTestingDataStorage;
import io.symeo.monolithic.backend.domain.bff.port.out.BffExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.TeamStorage;
import io.symeo.monolithic.backend.domain.bff.service.insights.TestingMetricsService;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
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
        final BffCommitTestingDataStorage commitTestingDataFacadeAdapter =
                mock(BffCommitTestingDataStorage.class);
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final TestingMetricsService testingMetricsService = new TestingMetricsService(teamStorage,
                commitTestingDataFacadeAdapter, bffExpositionStorageAdapter);

        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final UUID teamId = UUID.randomUUID();
        final Date startDate = stringToDate("2022-01-01");
        final Date endDate = stringToDate("2022-02-01");

        // When
        when(teamStorage.findById(teamId)).thenReturn(Optional.empty());
        final TestingMetrics optionalTestingMetrics =
                testingMetricsService.computeTestingMetricsForTeamIdFromStartDateToEndDate(organization, teamId
                        , startDate, endDate);

        // Then
        assertThat(optionalTestingMetrics).isNotNull();
        assertThat(optionalTestingMetrics.getCurrentStartDate()).isEqualTo(startDate);
        assertThat(optionalTestingMetrics.getCurrentEndDate()).isEqualTo(endDate);
        assertThat(optionalTestingMetrics.getPreviousStartDate()).isEqualTo("2021-12-01");
        assertThat(optionalTestingMetrics.getPreviousEndDate()).isEqualTo(startDate);
        assertThat(optionalTestingMetrics.getTestCount()).isEqualTo(null);
        assertThat(optionalTestingMetrics.getTestCountTendencyPercentage()).isEqualTo(null);
        assertThat(optionalTestingMetrics.getTestLineCount()).isEqualTo(null);
        assertThat(optionalTestingMetrics.getCodeLineCount()).isEqualTo(null);
        assertThat(optionalTestingMetrics.getTestToCodeRatio()).isEqualTo(null);
        assertThat(optionalTestingMetrics.getTestToCodeRatioTendencyPercentage()).isEqualTo(null);
        assertThat(optionalTestingMetrics.getCoverage()).isEqualTo(null);
        assertThat(optionalTestingMetrics.getCoverageTendencyPercentage()).isEqualTo(null);
    }

    @Test
    void should_return_empty_metrics_when_there_is_no_data() throws SymeoException {
        // Given
        final TeamStorage teamStorage = mock(TeamStorage.class);
        final BffCommitTestingDataStorage commitTestingDataFacadeAdapter =
                mock(BffCommitTestingDataStorage.class);
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final TestingMetricsService testingMetricsService = new TestingMetricsService(teamStorage,
                commitTestingDataFacadeAdapter, bffExpositionStorageAdapter);

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
        when(bffExpositionStorageAdapter.findAllRepositoriesForOrganizationIdAndTeamId(organization.getId(), teamId)).thenReturn(team.getRepositories());

        when(commitTestingDataFacadeAdapter.getLastTestingDataForRepoAndBranchAndDate(organization.getId(), repo1Name
                , repo1DefaultBranch, endDate))
                .thenReturn(Optional.empty());

        when(commitTestingDataFacadeAdapter.getLastTestingDataForRepoAndBranchAndDate(organization.getId(), repo2Name
                , repo2DefaultBranch, endDate))
                .thenReturn(Optional.empty());

        when(commitTestingDataFacadeAdapter.getLastTestingDataForRepoAndBranchAndDate(organization.getId(), repo1Name
                , repo1DefaultBranch, startDate))
                .thenReturn(Optional.empty());

        when(commitTestingDataFacadeAdapter.getLastTestingDataForRepoAndBranchAndDate(organization.getId(), repo2Name
                , repo2DefaultBranch, startDate))
                .thenReturn(Optional.empty());

        final TestingMetrics optionalTestingMetrics =
                testingMetricsService.computeTestingMetricsForTeamIdFromStartDateToEndDate(organization, teamId
                        , startDate, endDate);

        // Then
        assertThat(optionalTestingMetrics).isNotNull();
        assertThat(optionalTestingMetrics.getCurrentStartDate()).isEqualTo(startDate);
        assertThat(optionalTestingMetrics.getCurrentEndDate()).isEqualTo(endDate);
        assertThat(optionalTestingMetrics.getPreviousStartDate()).isEqualTo("2021-12-01");
        assertThat(optionalTestingMetrics.getPreviousEndDate()).isEqualTo(startDate);
        assertThat(optionalTestingMetrics.getTestCount()).isEqualTo(null);
        assertThat(optionalTestingMetrics.getTestCountTendencyPercentage()).isEqualTo(null);
        assertThat(optionalTestingMetrics.getTestLineCount()).isEqualTo(null);
        assertThat(optionalTestingMetrics.getCodeLineCount()).isEqualTo(null);
        assertThat(optionalTestingMetrics.getTestToCodeRatio()).isEqualTo(null);
        assertThat(optionalTestingMetrics.getTestToCodeRatioTendencyPercentage()).isEqualTo(null);
        assertThat(optionalTestingMetrics.getCoverage()).isEqualTo(null);
        assertThat(optionalTestingMetrics.getCoverageTendencyPercentage()).isEqualTo(null);
    }

    @Test
    void should_get_testing_metrics() throws SymeoException {
        // Given
        final TeamStorage teamStorage = mock(TeamStorage.class);
        final BffCommitTestingDataStorage commitTestingDataFacadeAdapter =
                mock(BffCommitTestingDataStorage.class);
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final TestingMetricsService testingMetricsService = new TestingMetricsService(teamStorage,
                commitTestingDataFacadeAdapter, bffExpositionStorageAdapter);

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
        when(bffExpositionStorageAdapter.findAllRepositoriesForOrganizationIdAndTeamId(organization.getId(), teamId)).thenReturn(team.getRepositories());

        when(commitTestingDataFacadeAdapter.getLastTestingDataForRepoAndBranchAndDate(organization.getId(), repo1Name
                , repo1DefaultBranch, endDate))
                .thenReturn(Optional.of(CommitTestingDataView.builder()
                        .unitTestCount(10)
                        .integrationTestCount(20)
                        .testLineCount(100)
                        .codeLineCount(300)
                        .totalBranchCount(100)
                        .coveredBranches(50)
                        .build()));

        when(commitTestingDataFacadeAdapter.getLastTestingDataForRepoAndBranchAndDate(organization.getId(), repo2Name
                , repo2DefaultBranch, endDate))
                .thenReturn(Optional.of(CommitTestingDataView.builder()
                        .unitTestCount(20)
                        .integrationTestCount(30)
                        .testLineCount(200)
                        .codeLineCount(600)
                        .totalBranchCount(200)
                        .coveredBranches(100)
                        .build()));

        when(commitTestingDataFacadeAdapter.getLastTestingDataForRepoAndBranchAndDate(organization.getId(), repo1Name
                , repo1DefaultBranch, startDate))
                .thenReturn(Optional.of(CommitTestingDataView.builder()
                        .unitTestCount(5)
                        .integrationTestCount(10)
                        .testLineCount(50)
                        .codeLineCount(150)
                        .totalBranchCount(100)
                        .coveredBranches(25)
                        .build()));

        when(commitTestingDataFacadeAdapter.getLastTestingDataForRepoAndBranchAndDate(organization.getId(), repo2Name
                , repo2DefaultBranch, startDate))
                .thenReturn(Optional.of(CommitTestingDataView.builder()
                        .unitTestCount(10)
                        .integrationTestCount(15)
                        .testLineCount(100)
                        .codeLineCount(300)
                        .totalBranchCount(200)
                        .coveredBranches(50)
                        .build()));

        final TestingMetrics optionalTestingMetrics =
                testingMetricsService.computeTestingMetricsForTeamIdFromStartDateToEndDate(organization, teamId
                        , startDate, endDate);

        // Then
        assertThat(optionalTestingMetrics).isNotNull();
        assertThat(optionalTestingMetrics.getCurrentStartDate()).isEqualTo(startDate);
        assertThat(optionalTestingMetrics.getCurrentEndDate()).isEqualTo(endDate);
        assertThat(optionalTestingMetrics.getPreviousStartDate()).isEqualTo("2021-12-01");
        assertThat(optionalTestingMetrics.getPreviousEndDate()).isEqualTo(startDate);
        assertThat(optionalTestingMetrics.getTestCount()).isEqualTo(80);
        assertThat(optionalTestingMetrics.getTestCountTendencyPercentage()).isEqualTo(100f);
        assertThat(optionalTestingMetrics.getTestLineCount()).isEqualTo(300);
        assertThat(optionalTestingMetrics.getCodeLineCount()).isEqualTo(900);
        assertThat(optionalTestingMetrics.getTestToCodeRatio()).isEqualTo(0.25f);
        assertThat(optionalTestingMetrics.getTestToCodeRatioTendencyPercentage()).isEqualTo(0f);
        assertThat(optionalTestingMetrics.getCoverage()).isEqualTo(50f);
        assertThat(optionalTestingMetrics.getCoverageTendencyPercentage()).isEqualTo(100f);
    }
}
