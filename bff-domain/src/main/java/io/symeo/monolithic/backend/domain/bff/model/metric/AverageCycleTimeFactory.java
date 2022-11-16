package io.symeo.monolithic.backend.domain.bff.model.metric;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

import static java.lang.Math.round;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.*;

@AllArgsConstructor
public class AverageCycleTimeFactory {

    public Optional<AverageCycleTime> computeAverageCycleTimeMetricsFromCycleTimeList(List<CycleTime> cycleTimes) {

        if (cycleTimes.isEmpty()) {
            return empty();
        }

        Long cumulatedCycleTimeValue = null;
        Long cumulatedCodingTime = null;
        Long cumulatedReviewTime = null;
        Long cumulatedTimeToDeploy = null;
        int numberOfCycleTime = 0;
        int numberOfCodingTime = 0;
        int numberOfReviewTime = 0;
        int numberOfTimeToDeploy = 0;

        for (CycleTime cycleTime : cycleTimes) {
            if (nonNull(cycleTime.getCodingTime())) {
                if (isNull(cumulatedCodingTime)) {
                    cumulatedCodingTime = cycleTime.getCodingTime();
                } else {
                    cumulatedCodingTime += cycleTime.getCodingTime();
                }
                numberOfCodingTime++;
            }
            if (nonNull(cycleTime.getReviewTime())) {
                if (isNull(cumulatedReviewTime)) {
                    cumulatedReviewTime = cycleTime.getReviewTime();
                } else {
                    cumulatedReviewTime += cycleTime.getReviewTime();
                }
                numberOfReviewTime++;
            }
            if (nonNull(cycleTime.getTimeToDeploy())) {
                if (isNull(cumulatedTimeToDeploy)) {
                    cumulatedTimeToDeploy = cycleTime.getTimeToDeploy();
                } else {
                    cumulatedTimeToDeploy += cycleTime.getTimeToDeploy();
                }
                numberOfTimeToDeploy++;
            }
            if (nonNull(cycleTime.getValue())) {
                if (isNull(cumulatedCycleTimeValue)) {
                    cumulatedCycleTimeValue = cycleTime.getValue();
                } else {
                    cumulatedCycleTimeValue += cycleTime.getValue();
                }
                numberOfCycleTime++;
            }
        }
        return of(AverageCycleTime.builder()
                .averageValue(averageValueWithOneDecimal(cumulatedCycleTimeValue, numberOfCycleTime))
                .averageCodingTime(averageValueWithOneDecimal(cumulatedCodingTime, numberOfCodingTime))
                .averageReviewTime(averageValueWithOneDecimal(cumulatedReviewTime, numberOfReviewTime))
                .averageTimeToDeploy(averageValueWithOneDecimal(cumulatedTimeToDeploy, numberOfTimeToDeploy))
                .build());
    }

    private Float averageValueWithOneDecimal(Long cumulatedValue, int size) {
        return isNull(cumulatedValue) ? null : round(10f * cumulatedValue / size) / 10f;
    }

}
