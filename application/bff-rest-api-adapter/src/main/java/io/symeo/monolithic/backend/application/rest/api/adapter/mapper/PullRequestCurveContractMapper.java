package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.bff.model.metric.curve.Curve;
import io.symeo.monolithic.backend.domain.bff.model.metric.curve.PullRequestPieceCurve;
import io.symeo.monolithic.backend.domain.bff.model.metric.curve.PullRequestPieceCurveWithAverage;
import io.symeo.monolithic.backend.frontend.contract.api.model.CurveDataResponseContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.CurveResponseContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.GetCurveResponseContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.PieceCurveDataResponseContract;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public interface PullRequestCurveContractMapper {

    static GetCurveResponseContract curveToContract(final PullRequestPieceCurveWithAverage pullRequestPieceCurveWithAverage) {
        final GetCurveResponseContract getCurveResponseContract = new GetCurveResponseContract();
        final CurveResponseContract curveResponseContract = new CurveResponseContract();
        final List<CurveDataResponseContract> curveDataResponseContractList = new ArrayList<>();
        final List<PieceCurveDataResponseContract> pieceCurve = new ArrayList<>();
        for (PullRequestPieceCurve.PullRequestPieceCurvePoint pullRequestPieceCurvePoint : pullRequestPieceCurveWithAverage.getPullRequestPieceCurve().getData()) {
            final PieceCurveDataResponseContract pieceCurveDataResponseContract = new PieceCurveDataResponseContract();
            pieceCurveDataResponseContract.setDate(pullRequestPieceCurvePoint.getDate());
            pieceCurveDataResponseContract.setOpen(pullRequestPieceCurvePoint.getOpen());
            pieceCurveDataResponseContract.setValue(new BigDecimal(Float.toString(pullRequestPieceCurvePoint.getValue())));
            pieceCurveDataResponseContract.setLabel(pullRequestPieceCurvePoint.getLabel());
            pieceCurveDataResponseContract.setLink(pullRequestPieceCurvePoint.getLink());
            pieceCurve.add(pieceCurveDataResponseContract);
        }
        for (Curve.CurvePoint curvePoint : pullRequestPieceCurveWithAverage.getAverageCurve().getData()) {
            final CurveDataResponseContract curveDataResponseContract = new CurveDataResponseContract();
            curveDataResponseContract.setDate(curvePoint.getDate());
            curveDataResponseContract.setValue(new BigDecimal(Float.toString(curvePoint.getValue())));
            curveDataResponseContractList.add(curveDataResponseContract);
        }
        curveResponseContract.setAverageCurve(curveDataResponseContractList);
        curveResponseContract.setPieceCurve(pieceCurve);
        curveResponseContract.setLimit(pullRequestPieceCurveWithAverage.getLimit());
        getCurveResponseContract.setCurves(curveResponseContract);
        return getCurveResponseContract;
    }

    static GetCurveResponseContract errorToContract(final SymeoException symeoException) {
        final GetCurveResponseContract getCurveResponseContract = new GetCurveResponseContract();
        getCurveResponseContract.setErrors(List.of(SymeoErrorContractMapper.exceptionToContract(symeoException)));
        return getCurveResponseContract;
    }



}
