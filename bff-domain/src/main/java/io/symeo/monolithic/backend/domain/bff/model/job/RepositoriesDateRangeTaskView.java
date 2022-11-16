package io.symeo.monolithic.backend.domain.bff.model.job;

import io.symeo.monolithic.backend.domain.bff.model.vcs.RepositoryView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RepositoriesDateRangeTaskView {
    List<RepositoryView> repositories;
    Date startDate;
    Date endDate;
}
