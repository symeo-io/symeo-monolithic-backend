package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.insight.curve.Curve;
import io.symeo.monolithic.backend.domain.model.insight.curve.LeadTimePieceCurve;
import io.symeo.monolithic.backend.domain.model.insight.curve.LeadTimePieceCurveWithAverage;
import io.symeo.monolithic.backend.frontend.contract.api.model.CurveDataResponseContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.LeadTimeCurveContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.LeadTimeCurveResponseContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.LeadTimePieceCurveDataResponseContract;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.LeadTimeContractMapper.floatToBigDecimal;

public interface LeadTimeCurveContractMapper {


    static LeadTimeCurveResponseContract errorToContract(final SymeoException symeoException) {
        final LeadTimeCurveResponseContract leadTimeCurveResponseContract = new LeadTimeCurveResponseContract();
        leadTimeCurveResponseContract.setErrors(List.of(SymeoErrorContractMapper.exceptionToContract(symeoException)));
        return leadTimeCurveResponseContract;
    }

    static LeadTimeCurveResponseContract toContract(final LeadTimePieceCurveWithAverage leadTimePieceCurveWithAverage) {
        final LeadTimeCurveResponseContract leadTimeCurveResponseContract = new LeadTimeCurveResponseContract();
        final LeadTimeCurveContract leadTimeCurveContract = new LeadTimeCurveContract();

        final List<CurveDataResponseContract> averageCurve = new ArrayList<>();
        for (Curve.CurvePoint curvePoint : leadTimePieceCurveWithAverage.getAverageCurve().getData()) {
            final CurveDataResponseContract curveDataResponseContract = new CurveDataResponseContract();
            curveDataResponseContract.setDate(curvePoint.getDate());
            curveDataResponseContract.setValue(floatToBigDecimal(curvePoint.getValue()));
            averageCurve.add(curveDataResponseContract);
        }
        leadTimeCurveContract.setAverageCurve(averageCurve);

        final List<LeadTimePieceCurveDataResponseContract> pieceCurve = new ArrayList<>();
        for (LeadTimePieceCurve.LeadTimePieceCurvePoint leadTimePieceCurvePoint :
                leadTimePieceCurveWithAverage.getLeadTimePieceCurve().getData()) {
            final LeadTimePieceCurveDataResponseContract leadTimePieceCurveDataResponseContract =
                    new LeadTimePieceCurveDataResponseContract();
            leadTimePieceCurveDataResponseContract.setDate(leadTimePieceCurvePoint.getDate());
            leadTimePieceCurveDataResponseContract.setValue(BigDecimal.valueOf(leadTimePieceCurvePoint.getValue()));
            leadTimePieceCurveDataResponseContract.setCodingTime(BigDecimal.valueOf(leadTimePieceCurvePoint.getCodingTime()));
            leadTimePieceCurveDataResponseContract.setReviewLag(BigDecimal.valueOf(leadTimePieceCurvePoint.getReviewLag()));
            leadTimePieceCurveDataResponseContract.setReviewTime(BigDecimal.valueOf(leadTimePieceCurvePoint.getReviewTime()));
            leadTimePieceCurveDataResponseContract.setDeployTime(BigDecimal.ZERO);
            leadTimePieceCurveDataResponseContract.setLabel(leadTimePieceCurvePoint.getLabel());
            leadTimePieceCurveDataResponseContract.setLink(leadTimePieceCurvePoint.getLink());
            pieceCurve.add(leadTimePieceCurveDataResponseContract);
        }
        leadTimeCurveContract.setPieceCurve(pieceCurve);

        leadTimeCurveResponseContract.setCurves(leadTimeCurveContract);
        return leadTimeCurveResponseContract;
    }
}
