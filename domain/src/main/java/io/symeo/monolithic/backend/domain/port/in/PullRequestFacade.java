package io.symeo.monolithic.backend.domain.port.in;

import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.insight.PullRequestHistogram;

public interface PullRequestFacade {
    PullRequestHistogram getHistogramForOrganizationAndTeamAndType(Organization organization, String teamName,
                                                                   String histogramType);

}
