package io.symeo.monolithic.backend.domain.bff.port.in;

import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.bff.core.Page;

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
