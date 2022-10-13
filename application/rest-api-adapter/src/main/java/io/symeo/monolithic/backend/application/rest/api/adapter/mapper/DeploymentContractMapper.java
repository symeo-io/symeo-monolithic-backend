package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.insight.DeploymentMetrics;
import io.symeo.monolithic.backend.frontend.contract.api.model.DeploymentResponseContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.DeploymentResponseContractDeployment;
import io.symeo.monolithic.backend.frontend.contract.api.model.MetricWithLabelContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.MetricsContract;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.dateToString;

public interface DeploymentContractMapper {

    static DeploymentResponseContract errorToContract(final SymeoException symeoException) {
        final DeploymentResponseContract deploymentResponseContract = new DeploymentResponseContract();
        deploymentResponseContract.setErrors(List.of(SymeoErrorContractMapper.exceptionToContract(symeoException)));
        return deploymentResponseContract;
    }

    static DeploymentResponseContract toContract(final Optional<DeploymentMetrics> deploymentMetrics) {
        final DeploymentResponseContract deploymentResponseContract = new DeploymentResponseContract();
        if (deploymentMetrics.isEmpty()) {
            return deploymentResponseContract;
        }
        final DeploymentResponseContractDeployment deployment =
                getDeploymentResponseContractDeployment(deploymentMetrics.get());
        deploymentResponseContract.setDeployment(deployment);
        return deploymentResponseContract;

    }

    private static DeploymentResponseContractDeployment getDeploymentResponseContractDeployment(DeploymentMetrics deploymentMetrics) {
        final DeploymentResponseContractDeployment deployment = new DeploymentResponseContractDeployment();
        mapDeployCount(deploymentMetrics, deployment);
        mapDeploysPerDay(deploymentMetrics, deployment);
        mapAverageTimeBetweenDeploys(deploymentMetrics, deployment);
        mapLastDeploy(deploymentMetrics, deployment);
        deployment.setPreviousStartDate(dateToString(deploymentMetrics.getPreviousStartDate()));
        deployment.setPreviousEndDate(dateToString(deploymentMetrics.getPreviousEndDate()));
        deployment.setCurrentStartDate(dateToString(deploymentMetrics.getCurrentStartDate()));
        deployment.setCurrentEndDate(dateToString(deploymentMetrics.getCurrentEndDate()));
        return deployment;
    }

    static void mapDeployCount(DeploymentMetrics deploymentMetrics, DeploymentResponseContractDeployment deployment) {
        final MetricsContract deployCount = new MetricsContract();
        deployCount.setValue(BigDecimal.valueOf(deploymentMetrics.getDeployCount()));
        deployCount.setTendencyPercentage(BigDecimal.valueOf(deploymentMetrics.getDeployCountTendencyPercentage()));
        deployment.setDeployCount(deployCount);
    }
    static void mapDeploysPerDay(DeploymentMetrics deploymentMetrics, DeploymentResponseContractDeployment deployment) {
        final MetricsContract deploysPerDay = new MetricsContract();
        deploysPerDay.setValue(BigDecimal.valueOf(deploymentMetrics.getDeploysPerDay()));
        deploysPerDay.setTendencyPercentage(BigDecimal.valueOf(deploymentMetrics.getDeploysPerDayTendencyPercentage()));
        deployment.setDeploysPerDay(deploysPerDay);
    }

    static void mapAverageTimeBetweenDeploys(DeploymentMetrics deploymentMetrics, DeploymentResponseContractDeployment deployment) {
        final MetricsContract averageTimeBetweenDeploys = new MetricsContract();
        averageTimeBetweenDeploys.setValue(BigDecimal.valueOf(0));
        averageTimeBetweenDeploys.setTendencyPercentage(BigDecimal.valueOf(0));
        deployment.setAverageTimeBetweenDeploys(averageTimeBetweenDeploys);
    }

    static void mapLastDeploy(DeploymentMetrics deploymentMetrics, DeploymentResponseContractDeployment deployment) {
        final MetricWithLabelContract lastDeploy = new MetricWithLabelContract();
        lastDeploy.setValue(BigDecimal.valueOf(0));
        lastDeploy.setLabel("TODO");
        deployment.setLastDeploy(lastDeploy);
    }
}
