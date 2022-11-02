package io.symeo.monolithic.backend.domain.bff.port.in;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.metric.TestingMetrics;
import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.util.Date;
import java.util.UUID;

public interface TestingMetricsFacadeAdapter {
    TestingMetrics computeTestingMetricsForTeamIdFromStartDateToEndDate(Organization organization,
                                                                                  UUID teamId,
                                                                                  Date startDate,
                                                                                  Date endDate) throws SymeoException;
}
