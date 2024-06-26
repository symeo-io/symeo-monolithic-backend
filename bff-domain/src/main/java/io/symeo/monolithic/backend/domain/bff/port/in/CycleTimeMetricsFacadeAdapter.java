package io.symeo.monolithic.backend.domain.bff.port.in;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTimeMetrics;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTimePiecePage;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public interface CycleTimeMetricsFacadeAdapter {
    Optional<CycleTimeMetrics> computeCycleTimeMetricsForTeamIdFromStartDateToEndDate(Organization organization,
                                                                                      UUID teamId,
                                                                                      Date startDate, Date endDate) throws SymeoException;
    CycleTimePiecePage computeCycleTimePiecesForTeamIdFromStartDateToEndDate(Organization organization,
                                                                             UUID teamId,
                                                                             Date startDate,
                                                                             Date endDate,
                                                                             Integer pageIndex,
                                                                             Integer pageSize,
                                                                             String sortBy,
                                                                             String sortDir) throws SymeoException;
}
