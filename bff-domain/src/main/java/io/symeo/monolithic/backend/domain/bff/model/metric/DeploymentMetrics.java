package io.symeo.monolithic.backend.domain.bff.model.metric;

import io.symeo.monolithic.backend.domain.helper.MetricsHelper;
import lombok.Builder;
import lombok.Value;

import java.util.Date;
import java.util.Optional;

import static io.symeo.monolithic.backend.domain.helper.MetricsHelper.*;
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
    Float lastDeployDuration;
    String lastDeployRepository;
    String lastDeployLink;

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
                        MetricsHelper.getTendencyPercentage((float) currentDeployment.getDeployCount(), (float) previousDeployment.getDeployCount())
                )
                .deploysPerDay(currentDeployment.getDeploysPerDay())
                .deploysPerDayTendencyPercentage(
                        MetricsHelper.getTendencyPercentage(currentDeployment.getDeploysPerDay(), previousDeployment.getDeploysPerDay())
                )
                .averageTimeBetweenDeploys(currentDeployment.getAverageTimeBetweenDeploys())
                .averageTimeBetweenDeploysTendencyPercentage(
                        MetricsHelper.getTendencyPercentage(currentDeployment.getAverageTimeBetweenDeploys(), previousDeployment.getAverageTimeBetweenDeploys())
                )
                .lastDeployDuration(currentDeployment.getLastDeployDuration())
                .lastDeployRepository(currentDeployment.getLastDeployRepository())
                .lastDeployLink(currentDeployment.getLastDeployLink())
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
                .lastDeployDuration(currentDeployment.getLastDeployDuration())
                .lastDeployRepository(currentDeployment.getLastDeployRepository())
                .lastDeployLink(currentDeployment.getLastDeployLink())
                .build());
    }
}
