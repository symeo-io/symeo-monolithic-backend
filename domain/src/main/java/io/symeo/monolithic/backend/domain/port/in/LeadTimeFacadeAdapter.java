package io.symeo.monolithic.backend.domain.port.in;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.insight.LeadTimeMetrics;
import io.symeo.monolithic.backend.domain.model.insight.curve.LeadTimePieceCurveWithAverage;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public interface LeadTimeFacadeAdapter {
    Optional<LeadTimeMetrics> computeLeadTimeMetricsForTeamIdFromStartDateToEndDate(Organization organization,
                                                                                    UUID teamId,
                                                                                    Date startDate, Date endDate) throws SymeoException;

    @Deprecated
    LeadTimePieceCurveWithAverage computeLeadTimeCurvesForTeamIdFromStartDateAndEndDate(UUID teamId, Date startDate,
                                                                                        Date endDate) throws SymeoException;
}
