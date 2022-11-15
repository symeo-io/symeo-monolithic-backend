package io.symeo.monolithic.backend.domain.bff.service.insights;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.Team;
import io.symeo.monolithic.backend.domain.bff.model.metric.CommitTestingDataView;
import io.symeo.monolithic.backend.domain.bff.model.metric.TestingMetrics;
import io.symeo.monolithic.backend.domain.bff.model.vcs.RepositoryView;
import io.symeo.monolithic.backend.domain.bff.port.in.TestingMetricsFacadeAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.BffCommitTestingDataStorage;
import io.symeo.monolithic.backend.domain.bff.port.out.BffExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.TeamStorage;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.helper.DateHelper;
import io.symeo.monolithic.backend.domain.helper.MetricsHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.lang.Math.round;
import static java.util.Objects.isNull;

@Slf4j
@AllArgsConstructor
public class TestingMetricsService implements TestingMetricsFacadeAdapter {

    private final TeamStorage teamStorage;
    private final BffCommitTestingDataStorage commitTestingDataFacadeAdapter;
    private final BffExpositionStorageAdapter bffExpositionStorageAdapter;
    @Override
    public TestingMetrics computeTestingMetricsForTeamIdFromStartDateToEndDate(Organization organization,
                                                                                            UUID teamId,
                                                                                            Date startDate,
                                                                                            Date endDate) throws SymeoException {
        final Date previousStartDate = DateHelper.getPreviousStartDateFromStartDateAndEndDate(startDate, endDate,
                organization.getTimeZone());
        Optional<Team> team = teamStorage.findById(teamId);

        if (team.isEmpty()) {
            return TestingMetrics.builder()
                    .hasData(false)
                    .currentStartDate(startDate)
                    .currentEndDate(endDate)
                    .previousStartDate(previousStartDate)
                    .previousEndDate(startDate)
                    .build();
        }

        List<RepositoryView> repositories = bffExpositionStorageAdapter.findAllRepositoriesForOrganizationIdAndTeamId(organization.getId(), teamId);

        Integer testCount = null;
        Integer testLineCount = null;
        Integer codeLineCount = null;
        Integer unitTestCount = null;
        Integer integrationTestCount = null;
        Integer endToEndTestCount = null;
        Integer totalBranchCount = null;
        Integer coveredBranches = null;

        Integer previousTestCount = null;
        Integer previousTestLineCount = null;
        Integer previousCodeLineCount = null;
        Integer previousUnitTestCount = null;
        Integer previousIntegrationTestCount = null;
        Integer previousEndToEndTestCount = null;
        Integer previousTotalBranchCount = null;
        Integer previousCoveredBranches = null;

        for (RepositoryView repository : repositories) {
            Optional<CommitTestingDataView> testingData = this.commitTestingDataFacadeAdapter.getLastTestingDataForRepoAndBranchAndDate(
                organization.getId(), repository.getName(), repository.getDefaultBranch(), endDate
            );
            Optional<CommitTestingDataView> previousTestingData = this.commitTestingDataFacadeAdapter.getLastTestingDataForRepoAndBranchAndDate(
                organization.getId(), repository.getName(), repository.getDefaultBranch(), startDate
            );

            if (testingData.isPresent()) {
                testCount = valueOrZero(testCount) + testingData.get().getUnitTestCount() + testingData.get().getIntegrationTestCount();
                testLineCount = valueOrZero(testLineCount) + testingData.get().getTestLineCount();
                codeLineCount = valueOrZero(codeLineCount) + testingData.get().getCodeLineCount();
                unitTestCount = valueOrZero(unitTestCount) + testingData.get().getUnitTestCount();
                integrationTestCount = valueOrZero(integrationTestCount) + testingData.get().getIntegrationTestCount();
                totalBranchCount = valueOrZero(totalBranchCount) + testingData.get().getTotalBranchCount();
                coveredBranches = valueOrZero(coveredBranches) + testingData.get().getCoveredBranches();
            }

            if (previousTestingData.isPresent()) {
                previousTestCount = valueOrZero(previousTestCount) + previousTestingData.get().getUnitTestCount() + previousTestingData.get().getIntegrationTestCount();
                previousTestLineCount = valueOrZero(previousTestLineCount) + previousTestingData.get().getTestLineCount();
                previousCodeLineCount = valueOrZero(previousCodeLineCount) + previousTestingData.get().getCodeLineCount();
                previousUnitTestCount = valueOrZero(previousUnitTestCount) + previousTestingData.get().getUnitTestCount();
                previousIntegrationTestCount = valueOrZero(previousIntegrationTestCount) + previousTestingData.get().getIntegrationTestCount();
                previousTotalBranchCount = valueOrZero(previousTotalBranchCount) + previousTestingData.get().getTotalBranchCount();
                previousCoveredBranches = valueOrZero(previousCoveredBranches) + previousTestingData.get().getCoveredBranches();
            }
        }

        return TestingMetrics.builder()
                .hasData(hasData)
                .currentStartDate(startDate)
                .currentEndDate(endDate)
                .previousStartDate(previousStartDate)
                .previousEndDate(startDate)
                .coverage(this.computeCoverage(coveredBranches, totalBranchCount))
                .coverageTendencyPercentage(MetricsHelper.getTendencyPercentage(this.computeCoverage(coveredBranches, totalBranchCount), this.computeCoverage(previousCoveredBranches, previousTotalBranchCount)))
                .testCount(testCount)
                .testCountTendencyPercentage(MetricsHelper.getTendencyPercentage(testCount, previousTestCount))
                .testLineCount(testLineCount)
                .codeLineCount(codeLineCount)
                .testToCodeRatio(this.computeTestToCodeRatio(testLineCount, codeLineCount))
                .testToCodeRatioTendencyPercentage(MetricsHelper.getTendencyPercentage(this.computeTestToCodeRatio(testLineCount, codeLineCount), this.computeTestToCodeRatio(previousTestLineCount, previousCodeLineCount)))
                .unitTestCount(unitTestCount)
                .unitTestCountTendencyPercentage(MetricsHelper.getTendencyPercentage(unitTestCount, previousUnitTestCount))
                .integrationTestCount(integrationTestCount)
                .integrationTestCountTendencyPercentage(MetricsHelper.getTendencyPercentage(integrationTestCount, previousIntegrationTestCount))
                .endToEndTestCount(endToEndTestCount)
                .endToEndTestCountTendencyPercentage(MetricsHelper.getTendencyPercentage(endToEndTestCount, previousEndToEndTestCount))
                .build();
    }

    private Float computeCoverage(Integer coveredBranches, Integer totalBranchCount) {
        if (isNull(coveredBranches) || isNull(totalBranchCount) || totalBranchCount.equals(0)) {
            return null;
        }

        return round(1000 * coveredBranches / totalBranchCount) / 10f;
    }

    private Float computeTestToCodeRatio(Integer testLineCount, Integer codeLineCount) {
        if (isNull(testLineCount) || isNull(codeLineCount) || (codeLineCount.equals(0) && testLineCount.equals(0))) {
            return null;
        }

        return (float) testLineCount / (codeLineCount + testLineCount);
    }

    private static Integer valueOrZero(Integer value) {
        return isNull(value) ? 0 : value;
    }
}
