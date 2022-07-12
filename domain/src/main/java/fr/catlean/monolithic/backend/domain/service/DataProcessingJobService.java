package fr.catlean.monolithic.backend.domain.service;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.PullRequest;
import fr.catlean.monolithic.backend.domain.model.account.OrganizationAccount;
import fr.catlean.monolithic.backend.domain.port.in.DataProcessingJobAdapter;
import fr.catlean.monolithic.backend.domain.port.out.OrganizationAccountAdapter;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class DataProcessingJobService implements DataProcessingJobAdapter {

    private final DeliveryProcessorService deliveryProcessorService;
    private final OrganizationAccountAdapter organizationAccountAdapter;
    private final PullRequestService pullRequestService;

    @Override
    public void start(final String organisationName) throws CatleanException {
        final OrganizationAccount organizationAccount =
                organizationAccountAdapter.findOrganizationForName(organisationName);
        final List<PullRequest> pullRequestList =
                deliveryProcessorService.collectPullRequestsForOrganization(organizationAccount);
        pullRequestService.computeAndSavePullRequestSizeHistogram(pullRequestList, organizationAccount);
        pullRequestService.computeAndSavePullRequestTimeHistogram(pullRequestList, organizationAccount);
    }
}
