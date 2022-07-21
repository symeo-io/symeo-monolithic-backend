package fr.catlean.monolithic.backend.domain.query;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram;
import fr.catlean.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HistogramQuery {

    private final ExpositionStorageAdapter expositionStorageAdapter;
    private final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter;

    public PullRequestHistogram readPullRequestHistogram(String organizationName, String teamName,
                                                         String histogramType) throws CatleanException {
        final Organization organization =
                accountOrganizationStorageAdapter.findVcsOrganizationForName(organizationName);
        return expositionStorageAdapter.readPullRequestHistogram(organization.getName(), teamName, histogramType);
    }
}
