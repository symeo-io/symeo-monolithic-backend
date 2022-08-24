package io.symeo.monolithic.backend.domain.service.platform.vcs;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.core.Page;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.port.in.PullRequestFacade;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.helper.pagination.PaginationHelper.*;

@AllArgsConstructor
public class PullRequestService implements PullRequestFacade {
    private final ExpositionStorageAdapter expositionStorageAdapter;

    @Override
    public Page<PullRequestView> getPullRequestViewsPageForTeamIdAndStartDateAndEndDateAndPaginationSorted(final UUID teamId,
                                                                                                           final Date startDate,
                                                                                                           final Date endDate,
                                                                                                           final Integer pageIndex,
                                                                                                           final Integer pageSize,
                                                                                                           final String sortingParameter,
                                                                                                           final String sortingDirection) throws SymeoException {
        validatePagination(pageIndex, pageSize);
        validateSortingInputs(sortingDirection, sortingParameter, PullRequestView.AVAILABLE_SORTING_PARAMETERS);
        final int count =
                expositionStorageAdapter.countPullRequestViewsForTeamIdAndStartDateAndEndDateAndPagination(teamId,
                        startDate, endDate);
        return Page.<PullRequestView>builder()
                .content(expositionStorageAdapter.readPullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted(teamId,
                        startDate, endDate, pageIndex, pageSize, sortingParameter, sortingDirection))
                .totalPageNumber(calculateTotalNumberOfPage(pageSize, count))
                .totalItemNumber(count)
                .build();
    }
}
