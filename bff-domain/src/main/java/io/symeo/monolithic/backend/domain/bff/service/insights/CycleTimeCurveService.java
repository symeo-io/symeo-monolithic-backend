package io.symeo.monolithic.backend.domain.bff.service.insights;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTime;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTimeFactory;
import io.symeo.monolithic.backend.domain.bff.model.metric.curve.CycleTimePieceCurveWithAverage;
import io.symeo.monolithic.backend.domain.bff.model.vcs.CommitView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.TagView;
import io.symeo.monolithic.backend.domain.bff.port.in.CycleTimeCurveFacadeAdapter;
import io.symeo.monolithic.backend.domain.bff.port.in.OrganizationSettingsFacade;
import io.symeo.monolithic.backend.domain.bff.port.out.BffExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.*;
import static java.util.Objects.isNull;
import static java.time.temporal.ChronoUnit.*;

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
        final List<CycleTime> cycleTimesForTeamIdBetweenStartDateAndEndDate =
                bffExpositionStorageAdapter.findCycleTimesForTeamIdBetweenStartDateAndEndDate(teamId, startDate, endDate)
                        .stream()
                        .map(cycleTime -> cycleTime.mapDeployDateToClosestRangeDate(rangeDates, cycleTime.getDeployDate()))
                        .toList();
        return CycleTimePieceCurveWithAverage.buildPullRequestCurve(cycleTimesForTeamIdBetweenStartDateAndEndDate);
    }
}
