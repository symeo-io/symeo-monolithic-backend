package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.insight.CycleTimeMetrics;
import io.symeo.monolithic.backend.domain.model.insight.CycleTimePiece;
import io.symeo.monolithic.backend.domain.model.insight.CycleTimePiecePage;
import io.symeo.monolithic.backend.frontend.contract.api.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.ContractMapperHelper.floatToBigDecimal;
import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.ContractMapperHelper.longToBigDecimal;
import static io.symeo.monolithic.backend.domain.helper.DateHelper.*;
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
        mapDeployTime(cycleTimeMetrics, cycleTime);
        cycleTime.setPreviousStartDate(dateToString(cycleTimeMetrics.getPreviousStartDate()));
        cycleTime.setPreviousEndDate(dateToString(cycleTimeMetrics.getPreviousEndDate()));
        cycleTime.setCurrentStartDate(dateToString(cycleTimeMetrics.getCurrentStartDate()));
        cycleTime.setCurrentEndDate(dateToString(cycleTimeMetrics.getCurrentEndDate()));
        return cycleTime;
    }

    static void mapDeployTime(CycleTimeMetrics cycleTimeMetrics, CycleTimeResponseContractCycleTime cycleTime) {
        final MetricsContract deployTime = new MetricsContract();
        deployTime.setValue(floatToBigDecimal(cycleTimeMetrics.getAverageDeployTime()));
        deployTime.setTendencyPercentage(floatToBigDecimal(cycleTimeMetrics.getAverageDeployTimePercentageTendency()));
        cycleTime.setTimeToDeploy(deployTime);
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
            cycleTimePieceContract.timeToDeploy(longToBigDecimal(cycleTimePiece.getDeployTime()));
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
}
