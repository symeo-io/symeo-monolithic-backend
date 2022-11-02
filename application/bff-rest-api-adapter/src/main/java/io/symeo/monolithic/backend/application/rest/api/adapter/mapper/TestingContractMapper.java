package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.bff.contract.api.model.*;
import io.symeo.monolithic.backend.domain.bff.model.metric.TestingMetrics;
import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.math.BigDecimal;
import java.util.List;

import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.ContractMapperHelper.floatToBigDecimal;
import static io.symeo.monolithic.backend.domain.helper.DateHelper.dateToString;
import static java.util.Objects.isNull;

public interface TestingContractMapper {
    static TestingResponseContract errorToContract(final SymeoException symeoException) {
        final TestingResponseContract testingResponseContract = new TestingResponseContract();
        testingResponseContract.setErrors(List.of(SymeoErrorContractMapper.exceptionToContract(symeoException)));
        return testingResponseContract;
    }

    static TestingResponseContract toContract(final TestingMetrics testingMetrics) {
        final TestingResponseContract testingResponseContract = new TestingResponseContract();
        final TestingResponseContractTesting testing =
                getTestingResponseContractTesting(testingMetrics);
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
        coverage.setValue(isNull(testingMetrics.getCoverage()) ? null : BigDecimal.valueOf(testingMetrics.getCoverage()));
        coverage.setTendencyPercentage(floatToBigDecimal(testingMetrics.getCoverageTendencyPercentage()));
        testing.setCoverage(coverage);
    }

    static void mapTestCount(TestingMetrics testingMetrics, TestingResponseContractTesting testing) {
        final MetricsContract testCount = new MetricsContract();
        testCount.setValue(isNull(testingMetrics.getTestCount()) ? null : BigDecimal.valueOf(testingMetrics.getTestCount()));
        testCount.setTendencyPercentage(floatToBigDecimal(testingMetrics.getTestCountTendencyPercentage()));
        testing.setTestCount(testCount);
    }

    static void mapTestToCodeRatio(TestingMetrics testingMetrics, TestingResponseContractTesting testing) {
        final TestToCodeRatioMetricsContract testToCodeRatio = new TestToCodeRatioMetricsContract();
        testToCodeRatio.setValue(isNull(testingMetrics.getTestToCodeRatio()) ? null : BigDecimal.valueOf(testingMetrics.getTestToCodeRatio()));
        testToCodeRatio.setTendencyPercentage(floatToBigDecimal(testingMetrics.getTestToCodeRatioTendencyPercentage()));
        testToCodeRatio.setCodeLineCount(isNull(testingMetrics.getCodeLineCount()) ? null : BigDecimal.valueOf(testingMetrics.getCodeLineCount()));
        testToCodeRatio.setTestLineCount(isNull(testingMetrics.getTestLineCount()) ? null : BigDecimal.valueOf(testingMetrics.getTestLineCount()));
        testing.setTestToCodeRatio(testToCodeRatio);
    }

    static void mapTestTypes(TestingMetrics testingMetrics, TestingResponseContractTesting testing) {
        final TestTypesMetricsContract testTypes = new TestTypesMetricsContract();
        testTypes.setUnit(isNull(testingMetrics.getUnitTestCount()) ? null : BigDecimal.valueOf(testingMetrics.getUnitTestCount()));
        testTypes.setUnitTendencyPercentage(isNull(testingMetrics.getUnitTestCountTendencyPercentage()) ? null : BigDecimal.valueOf(testingMetrics.getUnitTestCountTendencyPercentage()));
        testTypes.setIntegration(isNull(testingMetrics.getIntegrationTestCount()) ? null : BigDecimal.valueOf(testingMetrics.getIntegrationTestCount()));
        testTypes.setIntegrationTendencyPercentage(isNull(testingMetrics.getIntegrationTestCountTendencyPercentage()) ? null : BigDecimal.valueOf(testingMetrics.getIntegrationTestCountTendencyPercentage()));
        testTypes.setEndToEndTendencyPercentage(isNull(testingMetrics.getEndToEndTestCountTendencyPercentage()) ? null : BigDecimal.valueOf(testingMetrics.getEndToEndTestCountTendencyPercentage()));
        testing.setTestTypes(testTypes);
    }
}
