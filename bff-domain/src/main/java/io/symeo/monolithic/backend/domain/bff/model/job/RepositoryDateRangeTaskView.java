package io.symeo.monolithic.backend.domain.bff.model.job;

import io.symeo.monolithic.backend.domain.bff.model.vcs.RepositoryView;
import lombok.Builder;
import lombok.Value;

import java.util.Date;

@Value
@Builder
public class RepositoryDateRangeTaskView {
    RepositoryView repository;
    Date startDate;
    Date endDate;
}
