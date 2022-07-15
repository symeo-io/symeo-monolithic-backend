package fr.catlean.monolithic.backend.domain.query;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import fr.catlean.monolithic.backend.domain.port.out.OrganizationStorageAdapter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HistogramQuery {

    private final ExpositionStorageAdapter expositionStorageAdapter;
    private final OrganizationStorageAdapter organizationStorageAdapter;

    public PullRequestHistogram readPullRequestHistogram(String organizationName, String teamName,
                                                         String histogramType) throws CatleanException {
        final Organization organization =
                organizationStorageAdapter.findOrganizationForName(organizationName);
        return expositionStorageAdapter.readPullRequestHistogram(organization.getName(), teamName, histogramType);
    }
}
