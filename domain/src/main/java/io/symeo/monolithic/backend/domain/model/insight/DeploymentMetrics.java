package io.symeo.monolithic.backend.domain.model.insight;

import lombok.Builder;
import lombok.Value;

import java.util.Date;
import java.util.Optional;

import static java.lang.Math.round;
import static java.util.Objects.isNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;

@Builder
@Value
public class DeploymentMetrics {

    private static final Float EMPTY_PERCENTAGE = 0.0f;
    Date currentStartDate;
    Date currentEndDate;
    Date previousStartDate;
    Date previousEndDate;
    Integer deployCount;
    Float deployCountTendencyPercentage;
    Float deploysPerDay;
    Float deploysPerDayTendencyPercentage;
    Float averageTimeBetweenDeploys;
    Float averageTimeBetweenDeploysTendencyPercentage;
    Float lastDeploy;
    String lastDeployRepository;

    public static Optional<DeploymentMetrics> buildFromCurrentAndPreviousDeployment(Optional<Deployment> optionalCurrentDeployment,
                                                                                    Optional<Deployment> optionalPreviousDeployment,
                                                                                    Date previousStartDate,
                                                                                    Date startDate,
                                                                                    Date endDate) {

        if (optionalCurrentDeployment.isPresent() && optionalPreviousDeployment.isPresent()) {
            final Deployment currentDeployment = optionalCurrentDeployment.get();
            final Deployment previousDeployment = optionalPreviousDeployment.get();
            return getDeploymentMetricsForCurrentAndPreviousDeploymentPresents(currentDeployment,
                    previousDeployment, previousStartDate, startDate, endDate);
        } else if (optionalCurrentDeployment.isPresent()) {
            final Deployment currentDeployment = optionalCurrentDeployment.get();
            return getDeploymentMetricsWithNoPreviousDeployment(currentDeployment, previousStartDate,
                    startDate, endDate);
        } else {
            return empty();
        }
    }

    private static Optional<DeploymentMetrics> getDeploymentMetricsForCurrentAndPreviousDeploymentPresents(Deployment currentDeployment,
                                                                                                           Deployment previousDeployment,
                                                                                                           Date previousStartDate,
                                                                                                           Date startDate,
                                                                                                           Date endDate) {
        return of(DeploymentMetrics.builder()
                .currentStartDate(startDate)
                .currentEndDate(endDate)
                .previousStartDate(previousStartDate)
                .previousEndDate(startDate)
                .deployCount(currentDeployment.getDeployCount())
                .deployCountTendencyPercentage(
                        getTendencyPercentage((float) currentDeployment.getDeployCount(), (float) previousDeployment.getDeployCount())
                )
                .deploysPerDay(currentDeployment.getDeploysPerDay())
                .deploysPerDayTendencyPercentage(
                        getTendencyPercentage(currentDeployment.getDeploysPerDay(), previousDeployment.getDeploysPerDay())
                )
                .averageTimeBetweenDeploys(currentDeployment.getAverageTimeBetweenDeploys())
                .averageTimeBetweenDeploysTendencyPercentage(
                        getTendencyPercentage(currentDeployment.getAverageTimeBetweenDeploys(), previousDeployment.getAverageTimeBetweenDeploys())
                )
                .lastDeploy(currentDeployment.getLastDeploy())
                .lastDeployRepository(currentDeployment.getLastDeployRepository())
                .build());
    }

    private static Optional<DeploymentMetrics> getDeploymentMetricsWithNoPreviousDeployment(Deployment currentDeployment,
                                                                                            Date previousStartDate,
                                                                                            Date startDate,
                                                                                            Date endDate) {
        return of(DeploymentMetrics.builder()
                .currentStartDate(startDate)
                .currentEndDate(endDate)
                .previousStartDate(previousStartDate)
                .previousEndDate(startDate)
                .deployCount(currentDeployment.getDeployCount())
                .deployCountTendencyPercentage(EMPTY_PERCENTAGE)
                .deploysPerDay(currentDeployment.getDeploysPerDay())
                .deploysPerDayTendencyPercentage(EMPTY_PERCENTAGE)
                .averageTimeBetweenDeploys(currentDeployment.getAverageTimeBetweenDeploys())
                .averageTimeBetweenDeploysTendencyPercentage(EMPTY_PERCENTAGE)
                .lastDeploy(currentDeployment.getLastDeploy())
                .lastDeployRepository(currentDeployment.getLastDeployRepository())
                .build());
    }

    private static Float getTendencyPercentage(final Float currentValue, final Float previousValue) {
        if (isNull(currentValue) || isNull(previousValue)) {
            return null;
        }
        return round(previousValue == 0 ? 0 : 1000 * (currentValue - previousValue) / previousValue) / 10f;
    }
}
