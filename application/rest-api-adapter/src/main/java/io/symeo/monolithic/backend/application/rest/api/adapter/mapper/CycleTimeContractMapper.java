package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.insight.CycleTimeMetrics;
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

    Faker FAKER = new Faker();

    static CycleTimePiecesResponseContract toPiecesContract(final Date startDate, final Date endDate) {
        final CycleTimePiecesResponseContract cycleTimePiecesResponseContract = new CycleTimePiecesResponseContract();

        final CycleTimePiecesPageContract cycleTimePiecesPageContract = new CycleTimePiecesPageContract();
        final List<CycleTimePieceContract> pieces = getCycleTimePieceContracts(startDate, endDate);
        cycleTimePiecesPageContract.setPieces(
                pieces
        );
        final int numberOfPage = FAKER.number().numberBetween(1, 200);
        cycleTimePiecesPageContract.setTotalPageNumber(numberOfPage);
        cycleTimePiecesPageContract.setTotalItemNumber(pieces.size() * numberOfPage);
        cycleTimePiecesResponseContract.setPiecesPage(cycleTimePiecesPageContract);
        return cycleTimePiecesResponseContract;
    }

    private static List<CycleTimePieceContract> getCycleTimePieceContracts(final Date startDate, final Date endDate) {

        final ArrayList<CycleTimePieceContract> list = new ArrayList<>();
        for (int i = 0; i <= FAKER.number().numberBetween(5, 50); i++) {
            final CycleTimePieceContract cycleTimePieceContract = new CycleTimePieceContract();
            cycleTimePieceContract.author(FAKER.rickAndMorty().character());
            final double codingTime = FAKER.number().randomDouble(2, 1, 10000);
            cycleTimePieceContract.codingTime(BigDecimal.valueOf(codingTime));
            final double reviewTime = FAKER.number().randomDouble(2, 1, 10000);
            cycleTimePieceContract.reviewTime(BigDecimal.valueOf(reviewTime));
            final double timeToDeploy = FAKER.number().randomDouble(2, 1, 10000);
            cycleTimePieceContract.timeToDeploy(BigDecimal.valueOf(timeToDeploy));
            cycleTimePieceContract.status(FAKER.ancient().god());
            cycleTimePieceContract.id(FAKER.idNumber().valid());
            cycleTimePieceContract.cycleTime(BigDecimal.valueOf(codingTime + reviewTime + timeToDeploy));
            cycleTimePieceContract.setVcsRepository(FAKER.pokemon().name());
            cycleTimePieceContract.setVcsUrl("http://wwww.symeo.io");
            cycleTimePieceContract.setTitle(FAKER.harryPotter().character());
            cycleTimePieceContract.setCreationDate(ZonedDateTime.ofInstant(FAKER.date().between(startDate, endDate).toInstant(), ZoneId.systemDefault()));
            cycleTimePieceContract.setMergeDate(ZonedDateTime.ofInstant(FAKER.date().between(startDate, endDate).toInstant(), ZoneId.systemDefault()));
            list.add(cycleTimePieceContract);
        }
        return list;
    }
}
