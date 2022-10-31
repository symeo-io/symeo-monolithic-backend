package io.symeo.monolithic.backend.domain.bff.service.vcs;

import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.bff.port.in.PullRequestFacade;
import io.symeo.monolithic.backend.domain.bff.port.out.BffExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.helper.pagination.PaginationHelper;
import io.symeo.monolithic.backend.domain.bff.core.Page;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
public class PullRequestService implements PullRequestFacade {
    private final BffExpositionStorageAdapter bffExpositionStorageAdapter;

    @Override
    public Page<PullRequestView> getPullRequestViewsPageForTeamIdAndStartDateAndEndDateAndPaginationSorted(final UUID teamId,
                                                                                                           final Date startDate,
                                                                                                           final Date endDate,
                                                                                                           final Integer pageIndex,
                                                                                                           final Integer pageSize,
                                                                                                           final String sortingParameter,
                                                                                                           final String sortingDirection) throws SymeoException {
        PaginationHelper.validatePagination(pageIndex, pageSize);
        PaginationHelper.validateSortingInputs(sortingDirection, sortingParameter,
                PullRequestView.AVAILABLE_SORTING_PARAMETERS);
        final int count =
                bffExpositionStorageAdapter.countPullRequestViewsForTeamIdAndStartDateAndEndDateAndPagination(teamId,
                        startDate, endDate);
        return Page.<PullRequestView>builder()
                .content(bffExpositionStorageAdapter.readPullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted(teamId,
                        startDate, endDate, pageIndex, pageSize, sortingParameter, sortingDirection))
                .totalPageNumber(PaginationHelper.calculateTotalNumberOfPage(pageSize, count))
                .totalItemNumber(count)
                .build();
    }
}
