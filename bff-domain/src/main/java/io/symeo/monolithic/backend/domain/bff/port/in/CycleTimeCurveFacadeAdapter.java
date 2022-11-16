package io.symeo.monolithic.backend.domain.bff.port.in;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.metric.curve.CycleTimePieceCurveWithAverage;
import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.util.Date;
import java.util.UUID;

public interface CycleTimeCurveFacadeAdapter {

    CycleTimePieceCurveWithAverage computeCycleTimePieceCurveWithAverage(Organization organization,
                                                                         UUID teamId, Date startDate, Date endDate)
            throws SymeoException;
}
