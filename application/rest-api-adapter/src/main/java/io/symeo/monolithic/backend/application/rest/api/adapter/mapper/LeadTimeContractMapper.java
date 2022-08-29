package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.insight.LeadTimeMetrics;
import io.symeo.monolithic.backend.frontend.contract.api.model.LeadTimeResponseContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.LeadTimeResponseContractLeadTime;
import io.symeo.monolithic.backend.frontend.contract.api.model.MetricsContract;

import java.math.BigDecimal;
import java.util.List;

public interface LeadTimeContractMapper {


    static LeadTimeResponseContract errorToContract(final SymeoException symeoException) {
        final LeadTimeResponseContract leadTimeResponseContract = new LeadTimeResponseContract();
        leadTimeResponseContract.setErrors(List.of(SymeoErrorContractMapper.exceptionToContract(symeoException)));
        return leadTimeResponseContract;
    }

    static LeadTimeResponseContract toContract(final LeadTimeMetrics leadTimeMetrics) {
        final LeadTimeResponseContract leadTimeResponseContract = new LeadTimeResponseContract();
        final LeadTimeResponseContractLeadTime leadTime = getLeadTimeResponseContractLeadTime(leadTimeMetrics);
        leadTimeResponseContract.setLeadTime(leadTime);
        return leadTimeResponseContract;
    }

    private static LeadTimeResponseContractLeadTime getLeadTimeResponseContractLeadTime(LeadTimeMetrics leadTimeMetrics) {
        final LeadTimeResponseContractLeadTime leadTime = new LeadTimeResponseContractLeadTime();
        mapAverage(leadTimeMetrics, leadTime);
        mapCodingTime(leadTimeMetrics, leadTime);
        mapReviewLag(leadTimeMetrics, leadTime);
        mapReviewTime(leadTimeMetrics, leadTime);
        return leadTime;
    }

    private static void mapReviewTime(LeadTimeMetrics leadTimeMetrics, LeadTimeResponseContractLeadTime leadTime) {
        final MetricsContract reviewTime = new MetricsContract();
        reviewTime.setValue(BigDecimal.valueOf(leadTimeMetrics.getAverageReviewTime()));
        reviewTime.setTendencyPercentage(BigDecimal.valueOf(leadTimeMetrics.getAverageReviewTimePercentageTendency()));
        leadTime.setReviewTime(reviewTime);
    }

    private static void mapReviewLag(LeadTimeMetrics leadTimeMetrics, LeadTimeResponseContractLeadTime leadTime) {
        final MetricsContract reviewLag = new MetricsContract();
        reviewLag.setTendencyPercentage(BigDecimal.valueOf(leadTimeMetrics.getAverageReviewLagPercentageTendency()));
        reviewLag.setValue(BigDecimal.valueOf(leadTimeMetrics.getAverageReviewLag()));
        leadTime.setReviewLag(reviewLag);
    }

    private static void mapCodingTime(LeadTimeMetrics leadTimeMetrics, LeadTimeResponseContractLeadTime leadTime) {
        final MetricsContract codingTime = new MetricsContract();
        codingTime.setTendencyPercentage(BigDecimal.valueOf(leadTimeMetrics.getAverageCodingTimePercentageTendency()));
        codingTime.setValue(BigDecimal.valueOf(leadTimeMetrics.getAverageCodingTime()));
        leadTime.setCodingTime(codingTime);
    }

    private static void mapAverage(LeadTimeMetrics leadTimeMetrics, LeadTimeResponseContractLeadTime leadTime) {
        final MetricsContract average = new MetricsContract();
        average.setTendencyPercentage(BigDecimal.valueOf(leadTimeMetrics.getAverageTendencyPercentage()));
        average.setValue(BigDecimal.valueOf(leadTimeMetrics.getAverage()));
        leadTime.setAverage(average);
    }
}
