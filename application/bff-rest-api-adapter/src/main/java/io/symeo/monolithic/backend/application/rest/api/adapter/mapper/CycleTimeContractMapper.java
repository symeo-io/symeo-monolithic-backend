package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.domain.bff.model.metric.curve.Curve;
import io.symeo.monolithic.backend.domain.bff.model.metric.curve.CycleTimePieceCurve;
import io.symeo.monolithic.backend.domain.bff.model.metric.curve.CycleTimePieceCurveWithAverage;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTimeMetrics;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTimePiece;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTimePiecePage;
import io.symeo.monolithic.backend.bff.contract.api.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.ContractMapperHelper.floatToBigDecimal;
import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.ContractMapperHelper.longToBigDecimal;
import static io.symeo.monolithic.backend.domain.helper.DateHelper.dateTimeToString;
import static io.symeo.monolithic.backend.domain.helper.DateHelper.dateToString;

public interface CycleTimeContractMapper {


    static CycleTimeResponseContract errorToContract(final SymeoException symeoException) {
        final CycleTimeResponseContract cycleTimeResponseContract = new CycleTimeResponseContract();
        cycleTimeResponseContract.setErrors(List.of(SymeoErrorContractMapper.exceptionToContract(symeoException)));
        return cycleTimeResponseContract;
    }

    static CycleTimeResponseContract toContract(final Optional<CycleTimeMetrics> cycleTimeMetrics) {
        final CycleTimeResponseContract cycleTimeResponseContract = new CycleTimeResponseContract();
        if (cycleTimeMetrics.isEmpty()) {
            return cycleTimeResponseContract;
        }
        final CycleTimeResponseContractCycleTime cycleTime =
                getCycleTimeResponseContractCycleTime(cycleTimeMetrics.get());
        cycleTimeResponseContract.setCycleTime(cycleTime);
        return cycleTimeResponseContract;
    }

    private static CycleTimeResponseContractCycleTime getCycleTimeResponseContractCycleTime(CycleTimeMetrics cycleTimeMetrics) {
        final CycleTimeResponseContractCycleTime cycleTime = new CycleTimeResponseContractCycleTime();
        mapAverage(cycleTimeMetrics, cycleTime);
        mapCodingTime(cycleTimeMetrics, cycleTime);
        mapReviewTime(cycleTimeMetrics, cycleTime);
        mapTimeToDeploy(cycleTimeMetrics, cycleTime);
        cycleTime.setPreviousStartDate(dateToString(cycleTimeMetrics.getPreviousStartDate()));
        cycleTime.setPreviousEndDate(dateToString(cycleTimeMetrics.getPreviousEndDate()));
        cycleTime.setCurrentStartDate(dateToString(cycleTimeMetrics.getCurrentStartDate()));
        cycleTime.setCurrentEndDate(dateToString(cycleTimeMetrics.getCurrentEndDate()));
        return cycleTime;
    }

    static void mapTimeToDeploy(CycleTimeMetrics cycleTimeMetrics, CycleTimeResponseContractCycleTime cycleTime) {
        final MetricsContract timeToDeploy = new MetricsContract();
        timeToDeploy.setValue(floatToBigDecimal(cycleTimeMetrics.getAverageTimeToDeploy()));
        timeToDeploy.setTendencyPercentage(floatToBigDecimal(cycleTimeMetrics.getAverageTimeToDeployPercentageTendency()));
        cycleTime.setTimeToDeploy(timeToDeploy);
    }

    private static void mapReviewTime(CycleTimeMetrics cycleTimeMetrics, CycleTimeResponseContractCycleTime cycleTime) {
        final MetricsContract reviewTime = new MetricsContract();
        reviewTime.setValue(floatToBigDecimal(cycleTimeMetrics.getAverageReviewTime()));
        reviewTime.setTendencyPercentage(floatToBigDecimal(cycleTimeMetrics.getAverageReviewTimePercentageTendency()));
        cycleTime.setReviewTime(reviewTime);
    }

    private static void mapCodingTime(CycleTimeMetrics cycleTimeMetrics, CycleTimeResponseContractCycleTime cycleTime) {
        final MetricsContract codingTime = new MetricsContract();
        codingTime.setTendencyPercentage(floatToBigDecimal(cycleTimeMetrics.getAverageCodingTimePercentageTendency()));
        codingTime.setValue(floatToBigDecimal(cycleTimeMetrics.getAverageCodingTime()));
        cycleTime.setCodingTime(codingTime);
    }

    private static void mapAverage(CycleTimeMetrics cycleTimeMetrics, CycleTimeResponseContractCycleTime cycleTime) {
        final MetricsContract average = new MetricsContract();
        average.setTendencyPercentage(floatToBigDecimal(cycleTimeMetrics.getAverageTendencyPercentage()));
        average.setValue(floatToBigDecimal(cycleTimeMetrics.getAverage()));
        cycleTime.setAverage(average);
    }


    static CycleTimePiecesResponseContract toPiecesContract(CycleTimePiecePage cycleTimePiecePage) {
        final CycleTimePiecesResponseContract cycleTimePiecesResponseContract = new CycleTimePiecesResponseContract();
        final CycleTimePiecesPageContract cycleTimePiecesPageContract = new CycleTimePiecesPageContract();

        final List<CycleTimePieceContract> pieces = getCycleTimePieceContracts(cycleTimePiecePage.getCycleTimePieces());
        final int numberOfPage = cycleTimePiecePage.getTotalNumberOfPages();
        final int numberOfItems = cycleTimePiecePage.getTotalNumberOfPieces();

        cycleTimePiecesPageContract.setPieces(pieces);
        cycleTimePiecesPageContract.setTotalPageNumber(numberOfPage);
        cycleTimePiecesPageContract.setTotalItemNumber(numberOfItems);
        cycleTimePiecesResponseContract.setPiecesPage(cycleTimePiecesPageContract);
        return cycleTimePiecesResponseContract;
    }

    private static List<CycleTimePieceContract> getCycleTimePieceContracts(final List<CycleTimePiece> cycleTimePieces) {

        final ArrayList<CycleTimePieceContract> list = new ArrayList<>();
        for (CycleTimePiece cycleTimePiece : cycleTimePieces) {
            final CycleTimePieceContract cycleTimePieceContract = new CycleTimePieceContract();
            cycleTimePieceContract.author(cycleTimePiece.getAuthor());
            cycleTimePieceContract.codingTime(longToBigDecimal(cycleTimePiece.getCodingTime()));
            cycleTimePieceContract.reviewTime(longToBigDecimal(cycleTimePiece.getReviewTime()));
            cycleTimePieceContract.timeToDeploy(longToBigDecimal(cycleTimePiece.getTimeToDeploy()));
            cycleTimePieceContract.cycleTime(longToBigDecimal(cycleTimePiece.getCycleTime()));
            cycleTimePieceContract.status(cycleTimePiece.getState());
            cycleTimePieceContract.id(cycleTimePiece.getId());
            cycleTimePieceContract.setVcsRepository(cycleTimePiece.getRepository());
            cycleTimePieceContract.setVcsUrl(cycleTimePiece.getVcsUrl());
            cycleTimePieceContract.setTitle(cycleTimePiece.getTitle());
            cycleTimePieceContract.setCreationDate(dateTimeToString(cycleTimePiece.getCreationDate()));
            cycleTimePieceContract.setMergeDate(dateTimeToString(cycleTimePiece.getMergeDate()));
            list.add(cycleTimePieceContract);
        }
        return list;
    }

    static CycleTimeCurveResponseContract toCurveContract(CycleTimePieceCurveWithAverage cycleTimePieceCurveWithAverage) {
        final CycleTimeCurveResponseContract cycleTimeCurveResponseContract = new CycleTimeCurveResponseContract();
        final CycleTimeCurveContract curves = new CycleTimeCurveContract();
        final List<CurveDataResponseContract> averageCurve = new ArrayList<>();
        final List<CycleTimePieceCurveDataResponseContract> pieceCurve = new ArrayList<>();
        for (CycleTimePieceCurve.CyclePieceCurvePoint cycleTimePieceCurvePoint : cycleTimePieceCurveWithAverage.getCycleTimePieceCurve().getData()) {
            final CycleTimePieceCurveDataResponseContract cycleTimePieceCurveDataResponseContract = new CycleTimePieceCurveDataResponseContract();
            cycleTimePieceCurveDataResponseContract.setDate(cycleTimePieceCurvePoint.getDate());
            cycleTimePieceCurveDataResponseContract.setValue(longToBigDecimal(cycleTimePieceCurvePoint.getValue()));
            cycleTimePieceCurveDataResponseContract.setCodingTime(longToBigDecimal(cycleTimePieceCurvePoint.getCodingTime()));
            cycleTimePieceCurveDataResponseContract.setReviewTime(longToBigDecimal(cycleTimePieceCurvePoint.getReviewTime()));
            cycleTimePieceCurveDataResponseContract.setTimeToDeploy(longToBigDecimal(cycleTimePieceCurvePoint.getTimeToDeploy()));
            cycleTimePieceCurveDataResponseContract.label(cycleTimePieceCurvePoint.getLabel());
            cycleTimePieceCurveDataResponseContract.setLink(cycleTimePieceCurvePoint.getLink());
            pieceCurve.add(cycleTimePieceCurveDataResponseContract);
        }
        for (Curve.CurvePoint curvePoint : cycleTimePieceCurveWithAverage.getAverageCurve().getData()) {
            final CurveDataResponseContract curveDataResponseContract = new CurveDataResponseContract();
            curveDataResponseContract.setDate(curvePoint.getDate());
            curveDataResponseContract.setValue(floatToBigDecimal(curvePoint.getValue()));
            averageCurve.add(curveDataResponseContract);
        }
        curves.setAverageCurve(averageCurve);
        curves.setPieceCurve(pieceCurve);
        cycleTimeCurveResponseContract.setCurves(curves);
        return cycleTimeCurveResponseContract;
    }
}
