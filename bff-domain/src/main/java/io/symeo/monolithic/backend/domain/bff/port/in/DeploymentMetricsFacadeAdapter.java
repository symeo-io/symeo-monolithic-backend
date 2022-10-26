package io.symeo.monolithic.backend.domain.bff.port.in;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.metric.DeploymentMetrics;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public interface DeploymentMetricsFacadeAdapter {
    Optional<DeploymentMetrics> computeDeploymentMetricsForTeamIdFromStartDateToEndDate(Organization organization,
                                                                                        UUID teamId,
                                                                                        Date startDate,
                                                                                        Date endDate) throws SymeoException;
}
