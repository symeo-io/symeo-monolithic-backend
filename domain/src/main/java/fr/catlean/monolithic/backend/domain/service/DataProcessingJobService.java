package fr.catlean.monolithic.backend.domain.service;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.PullRequest;
import fr.catlean.monolithic.backend.domain.model.Repository;
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
    private final RepositoryService repositoryService;

    @Override
    public void start(final String organisationName) throws CatleanException {
        final Organization organization =
                organizationStorageAdapter.findOrganizationForName(organisationName);
        collectRepositories(organization);
        collectPullRequests(organization);
    }

    private void collectPullRequests(Organization organization) throws CatleanException {
        final List<PullRequest> pullRequestList =
                deliveryProcessorService.collectPullRequestsForOrganization(organization);
        pullRequestService.savePullRequests(pullRequestList);
        pullRequestService.computeAndSavePullRequestSizeHistogram(pullRequestList, organization);
        pullRequestService.computeAndSavePullRequestTimeHistogram(pullRequestList, organization);
    }

    private void collectRepositories(Organization organization) throws CatleanException {
        List<Repository> repositories = deliveryProcessorService.collectRepositoriesForOrganization(organization);
        repositories =
                repositories.stream().map(repository -> repository.toBuilder().organization(organization).build()).toList();
        repositoryService.saveRepositories(repositories);
    }
}
