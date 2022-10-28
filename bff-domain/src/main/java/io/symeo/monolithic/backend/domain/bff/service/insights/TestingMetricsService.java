package io.symeo.monolithic.backend.domain.bff.service.insights;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.Team;
import io.symeo.monolithic.backend.domain.bff.model.metric.TestingMetrics;
import io.symeo.monolithic.backend.domain.bff.model.vcs.RepositoryView;
import io.symeo.monolithic.backend.domain.bff.port.in.TestingMetricsFacadeAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.TeamStorage;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.helper.MetricsHelper;
import io.symeo.monolithic.backend.job.domain.model.testing.CommitTestingData;
import io.symeo.monolithic.backend.job.domain.port.in.CommitTestingDataFacadeAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;

@Slf4j
@AllArgsConstructor
public class TestingMetricsService implements TestingMetricsFacadeAdapter {

    private final TeamStorage teamStorage;
    private final CommitTestingDataFacadeAdapter commitTestingDataFacadeAdapter;
    @Override
    public Optional<TestingMetrics> computeTestingMetricsForTeamIdFromStartDateToEndDate(Organization organization,
                                                                                            UUID teamId,
                                                                                            Date startDate,
                                                                                            Date endDate) throws SymeoException {
        Optional<Team> team = teamStorage.findById(teamId);

        if (team.isEmpty()) {
            return Optional.empty();
        }

        List<RepositoryView> repositories = team.get().getRepositories();

        int testCount = 0;
        int testLineCount = 0;
        int codeLineCount = 0;
        int unitTestCount = 0;
        int integrationTestCount = 0;
        int endToEndTestCount = 0;
        int totalBranchCount = 0;
        int coveredBranches = 0;

        int previousTestCount = 0;
        int previousUnitTestCount = 0;
        int previousIntegrationTestCount = 0;
        int previousEndToEndTestCount = 0;
        int previousTotalBranchCount = 0;
        int previousCoveredBranches = 0;

        for (RepositoryView repository : repositories) {
            Optional<CommitTestingData> testingData = this.commitTestingDataFacadeAdapter.getLastTestingDataForRepoAndBranchAndDate(
                organization.getId(), repository.getName(), repository.getDefaultBranch(), endDate
            );
            Optional<CommitTestingData> previousTestingData = this.commitTestingDataFacadeAdapter.getLastTestingDataForRepoAndBranchAndDate(
                organization.getId(), repository.getName(), repository.getDefaultBranch(), startDate
            );

            if (testingData.isPresent()) {
                testCount += testingData.get().getUnitTestCount() + testingData.get().getIntegrationTestCount();
                testLineCount += testingData.get().getTestLineCount();
                codeLineCount += testingData.get().getCodeLineCount();
                unitTestCount += testingData.get().getUnitTestCount();
                integrationTestCount += testingData.get().getIntegrationTestCount();
                totalBranchCount += testingData.get().getCoverage().getTotalBranchCount();
                coveredBranches += testingData.get().getCoverage().getCoveredBranches();
            }

            if (previousTestingData.isPresent()) {
                previousTestCount += previousTestingData.get().getUnitTestCount() + previousTestingData.get().getIntegrationTestCount();
                previousUnitTestCount += previousTestingData.get().getUnitTestCount();
                previousIntegrationTestCount += previousTestingData.get().getIntegrationTestCount();
                previousTotalBranchCount += previousTestingData.get().getCoverage().getTotalBranchCount();
                previousCoveredBranches += previousTestingData.get().getCoverage().getCoveredBranches();
            }
        }

        return Optional.of(
            TestingMetrics.builder()
                .coverage(this.computeCoverage(coveredBranches, totalBranchCount))
                .coverageTendencyPercentage(MetricsHelper.getTendencyPercentage(this.computeCoverage(coveredBranches, totalBranchCount), this.computeCoverage(previousCoveredBranches, previousTotalBranchCount)))
                .testCount(testCount)
                .testCountTendencyPercentage(MetricsHelper.getTendencyPercentage(testCount, previousTestCount))
                .testLineCount(testLineCount)
                .codeLineCount(codeLineCount)
                .unitTestCount(unitTestCount)
                .unitTestCountTendencyPercentage(MetricsHelper.getTendencyPercentage(unitTestCount, previousUnitTestCount))
                .integrationTestCount(integrationTestCount)
                .integrationTestCountTendencyPercentage(MetricsHelper.getTendencyPercentage(integrationTestCount, previousIntegrationTestCount))
                .endToEndTestCount(endToEndTestCount)
                .endToEndTestCountTendencyPercentage(MetricsHelper.getTendencyPercentage(endToEndTestCount, previousEndToEndTestCount))
                .build()
        );
    }

    private Float computeCoverage(Integer coveredBranches, Integer totalBranchCount) {
        if (isNull(coveredBranches) || isNull(totalBranchCount) || totalBranchCount.equals(0)) {
            return null;
        }

        return (float) coveredBranches / totalBranchCount;
    }
}
