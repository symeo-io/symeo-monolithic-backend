package io.symeo.monolithic.backend.domain.bff.service.insights;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.metric.*;
import io.symeo.monolithic.backend.domain.bff.model.vcs.CommitView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.TagView;
import io.symeo.monolithic.backend.domain.bff.port.in.CycleTimeMetricsFacadeAdapter;
import io.symeo.monolithic.backend.domain.bff.port.in.OrganizationSettingsFacade;
import io.symeo.monolithic.backend.domain.bff.port.out.BffExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.*;
import static io.symeo.monolithic.backend.domain.helper.pagination.PaginationHelper.*;

@Slf4j
@AllArgsConstructor
public class CycleTimeMetricsService implements CycleTimeMetricsFacadeAdapter {

    private final BffExpositionStorageAdapter bffExpositionStorageAdapter;
    private final AverageCycleTimeFactory averageCycleTimeFactory;

    @Override
    public Optional<CycleTimeMetrics> computeCycleTimeMetricsForTeamIdFromStartDateToEndDate(final Organization organization,
                                                                                             final UUID teamId,
                                                                                             final Date startDate,
                                                                                             final Date endDate) throws SymeoException {

        final Date previousStartDate = getPreviousStartDateFromStartDateAndEndDate(startDate, endDate, organization.getTimeZone());

        final List<CycleTime> currentCycleTimesForTeamId =
                bffExpositionStorageAdapter.findCycleTimesForTeamIdBetweenStartDateAndEndDate(teamId, startDate, endDate);
        final Optional<AverageCycleTime> currentAverageCycleTimeMetrics =
                averageCycleTimeFactory.computeAverageCycleTimeMetricsFromCycleTimeList(currentCycleTimesForTeamId);

        final List<CycleTime> previousCycleTimesForTeamId =
                bffExpositionStorageAdapter.findCycleTimesForTeamIdBetweenStartDateAndEndDate(teamId, previousStartDate, startDate);
        final Optional<AverageCycleTime> previousAverageCycleTimeMetrics =
                averageCycleTimeFactory.computeAverageCycleTimeMetricsFromCycleTimeList(previousCycleTimesForTeamId);

        return CycleTimeMetrics.buildFromCurrentAndPreviousCycleTimes(currentAverageCycleTimeMetrics, previousAverageCycleTimeMetrics,
                previousStartDate, startDate,
                endDate);
    }

    @Override
    public CycleTimePiecePage computeCycleTimePiecesForTeamIdFromStartDateToEndDate(final Organization organization,
                                                                                    final UUID teamId,
                                                                                    final Date startDate,
                                                                                    final Date endDate,
                                                                                    final Integer pageIndex,
                                                                                    final Integer pageSize,
                                                                                    final String sortBy,
                                                                                    final String sortDir) throws SymeoException {
        final List<CycleTimePiece> cycleTimePiecesForTeamIdBetweenStartDateAndEndDate =
                bffExpositionStorageAdapter.findCycleTimePiecesForTeamIdBetweenStartDateAndEndDate(
                        teamId, startDate, endDate
                );
        validatePagination(pageIndex, pageSize);
        validateSortingInputs(sortDir, sortBy, CycleTimePiece.AVAILABLE_CYCLE_TIME_PIECE_SORTING_PARAMETERS);
        final List<CycleTimePiece> cycleTimePiecesForTeamIdBetweenStartDateAndEndDatePaginatedAndSorted =
                bffExpositionStorageAdapter.findCycleTimePiecesForTeamIdBetweenStartDateAndEndDatePaginatedAndSorted(
                        teamId, startDate, endDate, pageIndex, pageSize, sortBy, sortDir
                );

        return CycleTimePiecePage.builder()
                .totalNumberOfPieces(cycleTimePiecesForTeamIdBetweenStartDateAndEndDate.size())
                .totalNumberOfPages((int) Math.ceil(1.0f * cycleTimePiecesForTeamIdBetweenStartDateAndEndDate.size() / pageSize))
                .cycleTimePieces(cycleTimePiecesForTeamIdBetweenStartDateAndEndDatePaginatedAndSorted)
                .build();
    }
}
