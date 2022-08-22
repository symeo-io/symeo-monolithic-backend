package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.insight.Metrics;
import io.symeo.monolithic.backend.frontend.contract.api.model.MetricsContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.MetricsResponseContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.MetricsResponseContractMetrics;

import java.math.BigDecimal;
import java.util.List;

import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.SymeoErrorContractMapper.exceptionToContract;

public interface MetricsContractMapper {

    static MetricsResponseContract metricsToContract(final Metrics metrics) {
        final MetricsResponseContract metricsResponseContract = new MetricsResponseContract();
        final MetricsResponseContractMetrics metricsResponseContractMetrics = new MetricsResponseContractMetrics();
        final MetricsContract averageContract = new MetricsContract();
        averageContract.setValue(BigDecimal.valueOf(metrics.getCurrentAverage()));
        averageContract.setTendencyPercentage(BigDecimal.valueOf(metrics.getAverageTendency()));
        metricsResponseContractMetrics.setAverage(averageContract);
        final MetricsContract meetingGoal = new MetricsContract();
        meetingGoal.setValue(BigDecimal.valueOf(metrics.getCurrentPercentage()));
        meetingGoal.setTendencyPercentage(BigDecimal.valueOf(metrics.getPercentageTendency()));
        metricsResponseContractMetrics.setMeetingGoal(meetingGoal);
        metricsResponseContract.setMetrics(metricsResponseContractMetrics);
        return metricsResponseContract;
    }

    static MetricsResponseContract errorsToContract(final SymeoException symeoException) {
        final MetricsResponseContract metricsResponseContract = new MetricsResponseContract();
        metricsResponseContract.setErrors(List.of(exceptionToContract(symeoException)));
        return metricsResponseContract;
    }
}

