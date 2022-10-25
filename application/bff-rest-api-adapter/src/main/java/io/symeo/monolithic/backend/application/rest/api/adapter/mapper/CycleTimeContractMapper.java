package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTimeMetrics;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTimePiece;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTimePiecePage;
import io.symeo.monolithic.backend.frontend.contract.api.model.*;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.ContractMapperHelper.floatToBigDecimal;
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
            cycleTimePieceContract.codingTime(BigDecimal.valueOf(cycleTimePiece.getCodingTime()));
            cycleTimePieceContract.reviewTime(BigDecimal.valueOf(cycleTimePiece.getReviewTime()));
            cycleTimePieceContract.timeToDeploy(BigDecimal.valueOf(cycleTimePiece.getDeployTime()));
            cycleTimePieceContract.status(cycleTimePiece.getState());
            cycleTimePieceContract.id(cycleTimePiece.getId());
            cycleTimePieceContract.cycleTime(BigDecimal.valueOf(cycleTimePiece.getCycleTime()));
            cycleTimePieceContract.setVcsRepository(cycleTimePiece.getRepository());
            cycleTimePieceContract.setVcsUrl(cycleTimePiece.getVcsUrl());
            cycleTimePieceContract.setTitle(cycleTimePiece.getTitle());
            cycleTimePieceContract.setCreationDate(ZonedDateTime.ofInstant(cycleTimePiece.getCreationDate().toInstant(), ZoneId.systemDefault()));
            cycleTimePieceContract.setMergeDate(ZonedDateTime.ofInstant(cycleTimePiece.getMergeDate().toInstant(),
                    ZoneId.systemDefault()));
            list.add(cycleTimePieceContract);
        }
        return list;
    }

    Faker FAKER = new Faker();

    static CycleTimeCurveResponseContract toCurveContract(Date startDate, Date endDate) {
        final CycleTimeCurveResponseContract cycleTimeCurveResponseContract = new CycleTimeCurveResponseContract();
        final CycleTimeCurveContract curves = new CycleTimeCurveContract();
        final List<CurveDataResponseContract> averageCurve = new ArrayList<>();
        final List<CycleTimePieceCurveDataResponseContract> pieceCurve = new ArrayList<>();
        for (int i = 0; i < FAKER.number().numberBetween(10, 50); i++) {
            final CurveDataResponseContract curveDataResponseContract = new CurveDataResponseContract();
            curveDataResponseContract.setValue(BigDecimal.valueOf(FAKER.number().numberBetween(1, 200)));
            final String date = dateToString(FAKER.date().between(startDate, endDate));
            curveDataResponseContract.setDate(date);
            averageCurve.add(curveDataResponseContract);

            final CycleTimePieceCurveDataResponseContract piece =
                    new CycleTimePieceCurveDataResponseContract();
            piece.setDate(date);
            piece.setCodingTime(BigDecimal.valueOf(FAKER.number().numberBetween(1, 100)));
            piece.setReviewTime(BigDecimal.valueOf(FAKER.number().numberBetween(1, 100)));
            piece.setTimeToDeploy(BigDecimal.valueOf(FAKER.number().numberBetween(1, 100)));
            piece.setValue(BigDecimal.valueOf(FAKER.number().numberBetween(1, 100)));
            piece.setLabel(FAKER.rickAndMorty().character());
            piece.setLink("http://www.symeo.io");
            pieceCurve.add(piece);
        }
        curves.setAverageCurve(averageCurve);
        curves.setPieceCurve(pieceCurve);
        cycleTimeCurveResponseContract.setCurves(curves);
        return cycleTimeCurveResponseContract;
    }
}
