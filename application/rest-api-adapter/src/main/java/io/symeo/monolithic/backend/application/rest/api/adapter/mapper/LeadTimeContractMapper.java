package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.insight.LeadTimeMetrics;
import io.symeo.monolithic.backend.frontend.contract.api.model.LeadTimeResponseContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.LeadTimeResponseContractLeadTime;
import io.symeo.monolithic.backend.frontend.contract.api.model.MetricsContract;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface LeadTimeContractMapper {


    static LeadTimeResponseContract errorToContract(final SymeoException symeoException) {
        final LeadTimeResponseContract leadTimeResponseContract = new LeadTimeResponseContract();
        leadTimeResponseContract.setErrors(List.of(SymeoErrorContractMapper.exceptionToContract(symeoException)));
        return leadTimeResponseContract;
    }

    static LeadTimeResponseContract toContract(final Optional<LeadTimeMetrics> leadTimeMetrics) {
        final LeadTimeResponseContract leadTimeResponseContract = new LeadTimeResponseContract();
        if (leadTimeMetrics.isEmpty()) {
            return leadTimeResponseContract;
        }
        final LeadTimeResponseContractLeadTime leadTime = getLeadTimeResponseContractLeadTime(leadTimeMetrics.get());
        leadTimeResponseContract.setLeadTime(leadTime);
        return leadTimeResponseContract;
    }

    private static LeadTimeResponseContractLeadTime getLeadTimeResponseContractLeadTime(LeadTimeMetrics leadTimeMetrics) {
        final LeadTimeResponseContractLeadTime leadTime = new LeadTimeResponseContractLeadTime();
        mapAverage(leadTimeMetrics, leadTime);
        mapCodingTime(leadTimeMetrics, leadTime);
        mapReviewLag(leadTimeMetrics, leadTime);
        mapReviewTime(leadTimeMetrics, leadTime);
        mapDeployTime(leadTimeMetrics, leadTime);
        return leadTime;
    }

    static void mapDeployTime(LeadTimeMetrics leadTimeMetrics, LeadTimeResponseContractLeadTime leadTime) {
        final MetricsContract deployTime = new MetricsContract();
        deployTime.setValue(BigDecimal.ZERO);
        deployTime.setTendencyPercentage(BigDecimal.ZERO);
        leadTime.setTimeToDeploy(deployTime);
    }

    private static void mapReviewTime(LeadTimeMetrics leadTimeMetrics, LeadTimeResponseContractLeadTime leadTime) {
        final MetricsContract reviewTime = new MetricsContract();
        reviewTime.setValue(floatToBigDecimal(leadTimeMetrics.getAverageReviewTime()));
        reviewTime.setTendencyPercentage(floatToBigDecimal(leadTimeMetrics.getAverageReviewTimePercentageTendency()));
        leadTime.setReviewTime(reviewTime);
    }

    private static void mapReviewLag(LeadTimeMetrics leadTimeMetrics, LeadTimeResponseContractLeadTime leadTime) {
        final MetricsContract reviewLag = new MetricsContract();
        reviewLag.setTendencyPercentage(floatToBigDecimal(leadTimeMetrics.getAverageReviewLagPercentageTendency()));
        reviewLag.setValue(floatToBigDecimal(leadTimeMetrics.getAverageReviewLag()));
        leadTime.setReviewLag(reviewLag);
    }

    private static void mapCodingTime(LeadTimeMetrics leadTimeMetrics, LeadTimeResponseContractLeadTime leadTime) {
        final MetricsContract codingTime = new MetricsContract();
        codingTime.setTendencyPercentage(floatToBigDecimal(leadTimeMetrics.getAverageCodingTimePercentageTendency()));
        codingTime.setValue(floatToBigDecimal(leadTimeMetrics.getAverageCodingTime()));
        leadTime.setCodingTime(codingTime);
    }

    private static void mapAverage(LeadTimeMetrics leadTimeMetrics, LeadTimeResponseContractLeadTime leadTime) {
        final MetricsContract average = new MetricsContract();
        average.setTendencyPercentage(floatToBigDecimal(leadTimeMetrics.getAverageTendencyPercentage()));
        average.setValue(floatToBigDecimal(leadTimeMetrics.getAverage()));
        leadTime.setAverage(average);
    }

    private static BigDecimal floatToBigDecimal(final Float floatToConvert) {
        return new BigDecimal(Float.toString(floatToConvert));
    }
}
