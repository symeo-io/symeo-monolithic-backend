package io.symeo.monolithic.backend.domain.bff.service.insights;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTimeView;
import io.symeo.monolithic.backend.domain.bff.model.metric.curve.CycleTimePieceCurveWithAverage;
import io.symeo.monolithic.backend.domain.bff.port.in.CycleTimeCurveFacadeAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.BffExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.getRangeDatesBetweenStartDateAndEndDateForRange;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@AllArgsConstructor
public class CycleTimeCurveService implements CycleTimeCurveFacadeAdapter {

    private final BffExpositionStorageAdapter bffExpositionStorageAdapter;

    public CycleTimePieceCurveWithAverage computeCycleTimePieceCurveWithAverage(Organization organization,
                                                                                UUID teamId,
                                                                                Date startDate,
                                                                                Date endDate) throws SymeoException {

        final int range = 1;
        final List<Date> rangeDates = getRangeDatesBetweenStartDateAndEndDateForRange(startDate, endDate,
                range, organization.getTimeZone());
        final List<CycleTimeView> cycleTimesForTeamIdBetweenStartDateAndEndDateView =
                bffExpositionStorageAdapter.findCycleTimesForTeamIdBetweenStartDateAndEndDate(teamId, startDate,
                                endDate)
                        .stream()
                        .filter(cycleTime -> nonNull(cycleTime.getValue()))
                        .map(cycleTime -> cycleTime.mapDeployDateToClosestRangeDate(rangeDates,
                                isNull(cycleTime.getDeployDate()) ? cycleTime.getUpdateDate() :
                                        cycleTime.getDeployDate()))
                        .toList();
        return CycleTimePieceCurveWithAverage.buildPullRequestCurve(cycleTimesForTeamIdBetweenStartDateAndEndDateView);
    }
}
