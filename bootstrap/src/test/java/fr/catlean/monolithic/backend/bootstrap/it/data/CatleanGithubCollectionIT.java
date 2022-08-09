package fr.catlean.monolithic.backend.bootstrap.it.data;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.job.Job;
import fr.catlean.monolithic.backend.domain.job.runnable.CollectRepositoriesJobRunnable;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import fr.catlean.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import fr.catlean.monolithic.backend.domain.port.out.JobStorage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CatleanGithubCollectionIT extends AbstractCatleanDataCollectionIT {

    @Autowired
    public AccountOrganizationStorageAdapter accountOrganizationStorageAdapter;
    @Autowired
    public JobStorage jobStorage;

    @Test
    void should_collect_github_repositories_and_linked_pull_requests_for_a_given_organization() throws CatleanException {
        // Given
        final String organizationName = FAKER.ancient().god();
        final String organizationVcsId = FAKER.rickAndMorty().character();
        final Organization organization = Organization.builder()
                .vcsOrganization(VcsOrganization.builder().name(organizationName).vcsId(organizationVcsId).build())
                .id(UUID.randomUUID())
                .name(organizationName)
                .build();
        accountOrganizationStorageAdapter.createOrganization(organization);

        // When
        client.get()
                .uri(getApiURI(DATA_PROCESSING_JOB_REST_API_GET_START_JOB, "organization_name", organizationName))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();
        final List<Job> repositoriesJobs =
                jobStorage.findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(CollectRepositoriesJobRunnable.JOB_CODE, organization);
        assertThat(repositoriesJobs).hasSize(1);
    }
}
