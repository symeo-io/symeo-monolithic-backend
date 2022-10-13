package io.symeo.monolithic.backend.domain.port.in;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.insight.DeploymentMetrics;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public interface DeploymentMetricsFacadeAdapter {
    Optional<DeploymentMetrics> computeDeploymentMetricsForTeamIdFromStartDateToEndDate(Organization organization,
                                                                                        UUID teamId,
                                                                                        Date startDate,
                                                                                        Date endDate) throws SymeoException;
}
