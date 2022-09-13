package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.insight.Metrics;
import io.symeo.monolithic.backend.frontend.contract.api.model.MetricsContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.MetricsResponseContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.MetricsResponseContractMetrics;

import java.math.BigDecimal;
import java.util.List;

import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.SymeoErrorContractMapper.exceptionToContract;
import static io.symeo.monolithic.backend.domain.helper.DateHelper.dateToString;

public interface MetricsContractMapper {

    static MetricsResponseContract metricsToContract(final Metrics metrics) {
        final MetricsResponseContract metricsResponseContract = new MetricsResponseContract();
        final MetricsResponseContractMetrics metricsResponseContractMetrics = new MetricsResponseContractMetrics();
        final MetricsContract averageContract = getAverageContract(metrics);
        metricsResponseContractMetrics.setAverage(averageContract);
        final MetricsContract meetingGoal = getCurrentContract(metrics);
        metricsResponseContractMetrics.setMeetingGoal(meetingGoal);
        metricsResponseContractMetrics.setCurrentEndDate(dateToString(metrics.getCurrentEndDate()));
        metricsResponseContractMetrics.setCurrentStartDate(dateToString(metrics.getCurrentStartDate()));
        metricsResponseContractMetrics.setPreviousEndDate(dateToString(metrics.getPreviousEndDate()));
        metricsResponseContractMetrics.setPreviousStartDate(dateToString(metrics.getPreviousStartDate()));
        metricsResponseContract.setMetrics(metricsResponseContractMetrics);
        return metricsResponseContract;
    }

    private static MetricsContract getCurrentContract(Metrics metrics) {
        final MetricsContract currentContract = new MetricsContract();
        currentContract.setValue(BigDecimal.valueOf(metrics.getCurrentPercentage()));
        currentContract.setTendencyPercentage(BigDecimal.valueOf(metrics.getPercentageTendency()));

        return currentContract;
    }

    private static MetricsContract getAverageContract(Metrics metrics) {
        final MetricsContract averageContract = new MetricsContract();
        averageContract.setValue(BigDecimal.valueOf(metrics.getCurrentAverage()));
        averageContract.setTendencyPercentage(BigDecimal.valueOf(metrics.getAverageTendency()));
        return averageContract;
    }

    static MetricsResponseContract errorsToContract(final SymeoException symeoException) {
        final MetricsResponseContract metricsResponseContract = new MetricsResponseContract();
        metricsResponseContract.setErrors(List.of(exceptionToContract(symeoException)));
        return metricsResponseContract;
    }
}

