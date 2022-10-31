package io.symeo.monolithic.backend.domain.bff.model.job;

import io.symeo.monolithic.backend.domain.bff.model.vcs.RepositoryView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RepositoryDateRangeTaskView {
    RepositoryView repository;
    Date startDate;
    Date endDate;
}
