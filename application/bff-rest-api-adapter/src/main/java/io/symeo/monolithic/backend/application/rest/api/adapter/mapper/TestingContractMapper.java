package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.bff.contract.api.model.*;
import io.symeo.monolithic.backend.domain.bff.model.metric.TestingMetrics;
import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.ContractMapperHelper.floatToBigDecimal;
import static io.symeo.monolithic.backend.domain.helper.DateHelper.dateToString;

public interface TestingContractMapper {
    static TestingResponseContract errorToContract(final SymeoException symeoException) {
        final TestingResponseContract testingResponseContract = new TestingResponseContract();
        testingResponseContract.setErrors(List.of(SymeoErrorContractMapper.exceptionToContract(symeoException)));
        return testingResponseContract;
    }

    static TestingResponseContract toContract(final Optional<TestingMetrics> testingMetrics) {
        final TestingResponseContract testingResponseContract = new TestingResponseContract();
        if (testingMetrics.isEmpty()) {
            return testingResponseContract;
        }
        final TestingResponseContractTesting testing =
                getTestingResponseContractTesting(testingMetrics.get());
        testingResponseContract.setTesting(testing);
        return testingResponseContract;

    }

    private static TestingResponseContractTesting getTestingResponseContractTesting(TestingMetrics testingMetrics) {
        final TestingResponseContractTesting testing = new TestingResponseContractTesting();
        mapCoverage(testingMetrics, testing);
        mapTestCount(testingMetrics, testing);
        mapTestToCodeRatio(testingMetrics, testing);
        mapTestTypes(testingMetrics, testing);
        testing.setPreviousStartDate(dateToString(testingMetrics.getPreviousStartDate()));
        testing.setPreviousEndDate(dateToString(testingMetrics.getPreviousEndDate()));
        testing.setCurrentStartDate(dateToString(testingMetrics.getCurrentStartDate()));
        testing.setCurrentEndDate(dateToString(testingMetrics.getCurrentEndDate()));
        return testing;
    }

    static void mapCoverage(TestingMetrics testingMetrics, TestingResponseContractTesting testing) {
        final MetricsContract coverage = new MetricsContract();
        coverage.setValue(BigDecimal.valueOf(testingMetrics.getCoverage()));
        coverage.setTendencyPercentage(floatToBigDecimal(testingMetrics.getCoverageTendencyPercentage()));
        testing.setCoverage(coverage);
    }

    static void mapTestCount(TestingMetrics testingMetrics, TestingResponseContractTesting testing) {
        final MetricsContract testCount = new MetricsContract();
        testCount.setValue(BigDecimal.valueOf(testingMetrics.getTestCount()));
        testCount.setTendencyPercentage(floatToBigDecimal(testingMetrics.getTestCountTendencyPercentage()));
        testing.setTestCount(testCount);
    }

    static void mapTestToCodeRatio(TestingMetrics testingMetrics, TestingResponseContractTesting testing) {
        final TestToCodeRatioMetricsContract testToCodeRatio = new TestToCodeRatioMetricsContract();
        testToCodeRatio.setValue(BigDecimal.valueOf(testingMetrics.getTestCount()));
        testToCodeRatio.setTendencyPercentage(floatToBigDecimal(testingMetrics.getTestCountTendencyPercentage()));
        testToCodeRatio.setCodeLineCount(BigDecimal.valueOf(testingMetrics.getCodeLineCount()));
        testToCodeRatio.setTestLineCount(BigDecimal.valueOf(testingMetrics.getTestLineCount()));
        testing.setTestToCodeRatio(testToCodeRatio);
    }

    static void mapTestTypes(TestingMetrics testingMetrics, TestingResponseContractTesting testing) {
        final TestTypesMetricsContract testTypes = new TestTypesMetricsContract();
        testTypes.setUnit(BigDecimal.valueOf(testingMetrics.getUnitTestCount()));
        testTypes.setIntegration(BigDecimal.valueOf(testingMetrics.getIntegrationTestCount()));
        testTypes.setEndToEnd(BigDecimal.valueOf(testingMetrics.getEndToEndTestCount()));
        testing.setTestTypes(testTypes);
    }
}
