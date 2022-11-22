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

    public Optional<AverageCycleTime> computeAverageCycleTimeMetricsFromCycleTimeList(List<CycleTimeView> cycleTimeViews) {

        if (cycleTimeViews.isEmpty()) {
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

        for (CycleTimeView cycleTimeView : cycleTimeViews) {
            if (nonNull(cycleTimeView.getCodingTime())) {
                if (isNull(cumulatedCodingTime)) {
                    cumulatedCodingTime = cycleTimeView.getCodingTime();
                } else {
                    cumulatedCodingTime += cycleTimeView.getCodingTime();
                }
                numberOfCodingTime++;
            }
            if (nonNull(cycleTimeView.getReviewTime())) {
                if (isNull(cumulatedReviewTime)) {
                    cumulatedReviewTime = cycleTimeView.getReviewTime();
                } else {
                    cumulatedReviewTime += cycleTimeView.getReviewTime();
                }
                numberOfReviewTime++;
            }
            if (nonNull(cycleTimeView.getTimeToDeploy())) {
                if (isNull(cumulatedTimeToDeploy)) {
                    cumulatedTimeToDeploy = cycleTimeView.getTimeToDeploy();
                } else {
                    cumulatedTimeToDeploy += cycleTimeView.getTimeToDeploy();
                }
                numberOfTimeToDeploy++;
            }
            if (nonNull(cycleTimeView.getValue())) {
                if (isNull(cumulatedCycleTimeValue)) {
                    cumulatedCycleTimeValue = cycleTimeView.getValue();
                } else {
                    cumulatedCycleTimeValue += cycleTimeView.getValue();
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
