package io.symeo.monolithic.backend.domain.port.in;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.core.Page;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;

import java.util.Date;
import java.util.UUID;

public interface PullRequestFacade {

    Page<PullRequestView> getPullRequestViewsPageForTeamIdAndStartDateAndEndDateAndPaginationSorted(UUID teamId,
                                                                                                    Date startDate,
                                                                                                    Date endDate,
                                                                                                    Integer pageIndex,
                                                                                                    Integer pageSize,
                                                                                                    String sortingParameter,
                                                                                                    String sortingDirection)
            throws SymeoException;
}
