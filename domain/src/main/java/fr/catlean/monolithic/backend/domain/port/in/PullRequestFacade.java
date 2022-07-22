package fr.catlean.monolithic.backend.domain.port.in;

import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram;

public interface PullRequestFacade {
    PullRequestHistogram getHistogramForOrganizationAndTeamAndType(Organization organization, String teamName,
                                                                   String histogramType);

}
