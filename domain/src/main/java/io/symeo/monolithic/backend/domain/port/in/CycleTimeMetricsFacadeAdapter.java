package io.symeo.monolithic.backend.domain.port.in;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.insight.CycleTimeMetrics;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public interface CycleTimeMetricsFacadeAdapter {
    Optional<CycleTimeMetrics> computeCycleTimeMetricsForTeamIdFromStartDateToEndDate(Organization organization,
                                                                                      UUID teamId,
                                                                                      Date startDate, Date endDate) throws SymeoException;

}
