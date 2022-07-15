package fr.catlean.monolithic.backend.domain.service;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.PullRequest;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.port.in.DataProcessingJobAdapter;
import fr.catlean.monolithic.backend.domain.port.out.OrganizationStorageAdapter;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class DataProcessingJobService implements DataProcessingJobAdapter {

    private final DeliveryProcessorService deliveryProcessorService;
    private final OrganizationStorageAdapter organizationStorageAdapter;
    private final PullRequestService pullRequestService;

    @Override
    public void start(final String organisationName) throws CatleanException {
        final Organization organizationAccount =
                organizationStorageAdapter.findOrganizationForName(organisationName);
        final List<PullRequest> pullRequestList =
                deliveryProcessorService.collectPullRequestsForOrganization(organizationAccount);
        pullRequestService.computeAndSavePullRequestSizeHistogram(pullRequestList, organizationAccount);
        pullRequestService.computeAndSavePullRequestTimeHistogram(pullRequestList, organizationAccount);
    }
}
