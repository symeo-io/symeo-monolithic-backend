package fr.catlean.monolithic.backend.domain.query;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HistogramQuery {

    private final ExpositionStorageAdapter expositionStorageAdapter;

    public PullRequestHistogram readPullRequestHistogram(final Organization organization, String teamName,
                                                         String histogramType) throws CatleanException {
        return expositionStorageAdapter.readPullRequestHistogram(organization.getId().toString(),
                teamName, histogramType);
    }
}
