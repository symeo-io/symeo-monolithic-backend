package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.insight.curve.Curve;
import io.symeo.monolithic.backend.domain.model.insight.curve.PieceCurve;
import io.symeo.monolithic.backend.domain.model.insight.curve.PieceCurveWithAverage;
import io.symeo.monolithic.backend.frontend.contract.api.model.CurveDataResponseContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.CurveResponseContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.GetCurveResponseContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.PieceCurveDataResponseContract;

import java.util.ArrayList;
import java.util.List;

public interface CurveMapper {

    static GetCurveResponseContract curveToContract(final PieceCurveWithAverage pieceCurveWithAverage) {
        final GetCurveResponseContract getCurveResponseContract = new GetCurveResponseContract();
        final CurveResponseContract curveResponseContract = new CurveResponseContract();
        final List<CurveDataResponseContract> curveDataResponseContractList = new ArrayList<>();
        final List<PieceCurveDataResponseContract> pieceCurve = new ArrayList<>();
        for (PieceCurve.PieceCurvePoint pieceCurvePoint : pieceCurveWithAverage.getPieceCurve().getData()) {
            final PieceCurveDataResponseContract pieceCurveDataResponseContract = new PieceCurveDataResponseContract();
            pieceCurveDataResponseContract.setDate(pieceCurvePoint.getDate());
            pieceCurveDataResponseContract.setOpen(pieceCurvePoint.getOpen());
            pieceCurveDataResponseContract.setValue(pieceCurvePoint.getValue() == 0 ? 1 : pieceCurvePoint.getValue());
            pieceCurve.add(pieceCurveDataResponseContract);
        }
        for (Curve.CurvePoint curvePoint : pieceCurveWithAverage.getAverageCurve().getData()) {
            final CurveDataResponseContract curveDataResponseContract = new CurveDataResponseContract();
            curveDataResponseContract.setDate(curvePoint.getDate());
            curveDataResponseContract.setValue(curvePoint.getValue() == 0 ? 1 : curvePoint.getValue());
            curveDataResponseContractList.add(curveDataResponseContract);
        }
        curveResponseContract.setAverageCurve(curveDataResponseContractList);
        curveResponseContract.setPieceCurve(pieceCurve);
        curveResponseContract.setLimit(pieceCurveWithAverage.getLimit());
        getCurveResponseContract.setCurves(curveResponseContract);
        return getCurveResponseContract;
    }

    static GetCurveResponseContract errorToContract(final SymeoException symeoException) {
        final GetCurveResponseContract getCurveResponseContract = new GetCurveResponseContract();
        getCurveResponseContract.setErrors(List.of(SymeoErrorContractMapper.exceptionToContract(symeoException)));
        return getCurveResponseContract;
    }


}
