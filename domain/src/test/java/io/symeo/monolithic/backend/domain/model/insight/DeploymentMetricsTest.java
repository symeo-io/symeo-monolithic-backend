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
                        .deploysPerDay(0.1f)
                        .averageTimeBetweenDeploys(4.6f)
                        .lastDeploy(1.4f)
                        .lastDeployRepository("test-repo-1")
                        .build());
        final Optional<Deployment> previousDeployment =
                Optional.of(Deployment.builder()
                        .deployCount(5)
                        .deploysPerDay(0.3f)
                        .averageTimeBetweenDeploys(3.2f)
                        .lastDeploy(0.7f)
                        .lastDeployRepository("test-repo-2")
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
                        .deploysPerDay(0.1f)
                        .deploysPerDayTendencyPercentage(0.0f)
                        .averageTimeBetweenDeploys(4.6f)
                        .averageTimeBetweenDeploysTendencyPercentage(0.0f)
                        .lastDeploy(1.4f)
                        .lastDeployRepository("test-repo-1")
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
                        .deploysPerDay(0.1f)
                        .deploysPerDayTendencyPercentage(-66.7f)
                        .averageTimeBetweenDeploys(4.6f)
                        .averageTimeBetweenDeploysTendencyPercentage(43.7f)
                        .lastDeploy(1.4f)
                        .lastDeployRepository("test-repo-1")
                        .build()
        );
    }
}
