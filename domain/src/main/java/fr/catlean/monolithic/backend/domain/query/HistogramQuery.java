package fr.catlean.monolithic.backend.domain.query;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.OrganizationAccount;
import fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import fr.catlean.monolithic.backend.domain.port.out.OrganizationAccountAdapter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HistogramQuery {

    private final ExpositionStorageAdapter expositionStorageAdapter;
    private final OrganizationAccountAdapter organizationAccountAdapter;

    public PullRequestHistogram readPullRequestHistogram(String organizationName, String teamName,
                                                         String histogramType) throws CatleanException {
        final OrganizationAccount organization =
                organizationAccountAdapter.findOrganizationForName(organizationName);
        return expositionStorageAdapter.readPullRequestHistogram(organization.getName(), teamName, histogramType);
    }
}