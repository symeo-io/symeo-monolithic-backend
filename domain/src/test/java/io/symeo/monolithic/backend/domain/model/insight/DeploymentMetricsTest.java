package io.symeo.monolithic.backend.domain.model.insight;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Optional;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class DeploymentMetricsTest {

    @Test
    void should_build_deployment_metrics_given_current_and_previous_deployment() throws SymeoException {
        // Given
        final Date previousStartDate = stringToDate("2022-01-01");
        final Date startDate = stringToDate("2022-01-15");
        final Date endDate = stringToDate("2022-01-29");

        final Optional<Deployment> emptyCurrentDeployment = Optional.empty();
        final Optional<Deployment> emptyPreviousDeployment = Optional.empty();

        final Optional<Deployment> currentDeployment =
                Optional.of(Deployment.builder()
                        .deployCount(20)
                        .build());
        final Optional<Deployment> previousDeployment =
                Optional.of(Deployment.builder()
                        .deployCount(5)
                        .build());

        // When
        final Optional<DeploymentMetrics> deploymentMetricsForEmptyCurrentAndPreviousDeployment =
                DeploymentMetrics.buildFromCurrentAndPreviousDeployment(
                        emptyCurrentDeployment,
                        emptyPreviousDeployment,
                        previousStartDate,
                        startDate,
                        endDate
                );
        final Optional<DeploymentMetrics> deploymentMetricsForEmptyPreviousDeployment =
                DeploymentMetrics.buildFromCurrentAndPreviousDeployment(
                        currentDeployment,
                        emptyPreviousDeployment,
                        previousStartDate,
                        startDate,
                        endDate
                );

        final Optional<DeploymentMetrics> deploymentMetricsForEmptyCurrentDeployment =
                DeploymentMetrics.buildFromCurrentAndPreviousDeployment(
                        emptyCurrentDeployment,
                        emptyPreviousDeployment,
                        previousStartDate,
                        startDate,
                        endDate
                );
        final Optional<DeploymentMetrics> deploymentMetrics =
                DeploymentMetrics.buildFromCurrentAndPreviousDeployment(
                        currentDeployment,
                        previousDeployment,
                        previousStartDate,
                        startDate,
                        endDate
                );

        // Then
        assertThat(deploymentMetricsForEmptyCurrentAndPreviousDeployment).isEmpty();
        assertThat(deploymentMetricsForEmptyCurrentDeployment).isEmpty();

        assertThat(deploymentMetricsForEmptyPreviousDeployment).isPresent();
        assertThat(deploymentMetricsForEmptyPreviousDeployment.get()).isEqualTo(
                DeploymentMetrics.builder()
                        .currentStartDate(startDate)
                        .currentEndDate(endDate)
                        .previousStartDate(previousStartDate)
                        .previousEndDate(startDate)
                        .deployCount(20)
                        .deployCountTendencyPercentage(0.0f)
                        .deploysPerDay(null)
                        .deploysPerDayTendencyPercentage(0.0f)
                        .averageTimeBetweenDeploys(null)
                        .averageTimeBetweenDeploysTendencyPercentage(0.0f)
                        .lastDeploy(null)
                        .lastDeployRepository(null)
                        .build()
        );

        assertThat(deploymentMetrics).isPresent();
        assertThat(deploymentMetrics.get()).isEqualTo(
                DeploymentMetrics.builder()
                        .currentStartDate(startDate)
                        .currentEndDate(endDate)
                        .previousStartDate(previousStartDate)
                        .previousEndDate(startDate)
                        .deployCount(20)
                        .deployCountTendencyPercentage(300f)
                        .build()
        );
    }
}
