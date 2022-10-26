package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.bff.model.metric.DeploymentMetrics;
import io.symeo.monolithic.backend.bff.contract.api.model.DeploymentResponseContract;
import io.symeo.monolithic.backend.bff.contract.api.model.DeploymentResponseContractDeployment;
import io.symeo.monolithic.backend.bff.contract.api.model.MetricWithLabelAndUrlContract;
import io.symeo.monolithic.backend.bff.contract.api.model.MetricsContract;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.ContractMapperHelper.floatToBigDecimal;
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
        deployCount.setTendencyPercentage(floatToBigDecimal(deploymentMetrics.getDeployCountTendencyPercentage()));
        deployment.setDeployCount(deployCount);
    }

    static void mapDeploysPerDay(DeploymentMetrics deploymentMetrics, DeploymentResponseContractDeployment deployment) {
        final MetricsContract deploysPerDay = new MetricsContract();
        deploysPerDay.setValue(floatToBigDecimal(deploymentMetrics.getDeploysPerDay()));
        deploysPerDay.setTendencyPercentage(floatToBigDecimal(deploymentMetrics.getDeploysPerDayTendencyPercentage()));
        deployment.setDeploysPerDay(deploysPerDay);
    }

    static void mapAverageTimeBetweenDeploys(DeploymentMetrics deploymentMetrics, DeploymentResponseContractDeployment deployment) {
        final MetricsContract averageTimeBetweenDeploys = new MetricsContract();
        averageTimeBetweenDeploys.setValue(floatToBigDecimal(deploymentMetrics.getAverageTimeBetweenDeploys()));
        averageTimeBetweenDeploys.setTendencyPercentage(floatToBigDecimal(deploymentMetrics.getAverageTimeBetweenDeploysTendencyPercentage()));
        deployment.setAverageTimeBetweenDeploys(averageTimeBetweenDeploys);
    }

    static void mapLastDeploy(DeploymentMetrics deploymentMetrics, DeploymentResponseContractDeployment deployment) {
        final MetricWithLabelAndUrlContract lastDeploy = new MetricWithLabelAndUrlContract();
        lastDeploy.setValue(floatToBigDecimal(deploymentMetrics.getLastDeployDuration()));
        lastDeploy.setLabel(deploymentMetrics.getLastDeployRepository());
        lastDeploy.setLink(deploymentMetrics.getLastDeployLink());
        deployment.setLastDeploy(lastDeploy);
    }
}
