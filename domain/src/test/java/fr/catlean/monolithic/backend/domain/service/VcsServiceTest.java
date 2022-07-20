package fr.catlean.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.command.DeliveryCommand;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.VcsConfiguration;
import fr.catlean.monolithic.backend.domain.query.DeliveryQuery;
import fr.catlean.monolithic.backend.domain.service.platform.vcs.VcsService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VcsServiceTest {

    private final Faker faker = Faker.instance();

    @Test
    void should_return_empty_list_for_repository_without_pull_request() throws CatleanException {
        // Given
        final DeliveryCommand deliveryCommand = mock(DeliveryCommand.class);
        final DeliveryQuery deliveryQuery = mock(DeliveryQuery.class);
        final VcsService vcsService = new VcsService(deliveryCommand,
                deliveryQuery);
        final String organizationName = faker.name().name();
        final Organization organizationAccount = Organization.builder()
                .vcsConfiguration(VcsConfiguration.builder().build()).name(organizationName).build();

        // When
        final Repository repo1 =
                Repository.builder().name(faker.pokemon().name() + "1").vcsOrganizationName(organizationName).build();
        final Repository repo2 =
                Repository.builder().name(faker.pokemon().name() + "2").vcsOrganizationName(organizationName).build();
        when(deliveryQuery.readRepositoriesForOrganization(organizationAccount))
                .thenReturn(
                        List.of(
                                repo1,
                                repo2
                        )
                );
        final PullRequest pr11 = PullRequest.builder().id("github-11").build();
        final PullRequest pr12 = PullRequest.builder().id("github-12").build();
        when(deliveryQuery.readPullRequestsForRepository(repo1))
                .thenReturn(
                        List.of(
                                pr11,
                                pr12
                        )
                );
        when(deliveryQuery.readPullRequestsForRepository(repo2))
                .thenReturn(List.of());
        final List<PullRequest> pullRequestList =
                vcsService.collectPullRequestsForOrganization(organizationAccount);

        // Then
        assertThat(pullRequestList).containsAll(List.of(pr11, pr12));
    }


    @Test
    void should_collect_repositories_given_an_organization() throws CatleanException {
        // Given
        final DeliveryCommand deliveryCommand = mock(DeliveryCommand.class);
        final DeliveryQuery deliveryQuery = mock(DeliveryQuery.class);
        final VcsService vcsService = new VcsService(deliveryCommand,
                deliveryQuery);
        final String organizationName = faker.name().name();
        final Organization organization = Organization.builder()
                .vcsConfiguration(VcsConfiguration.builder().build()).name(organizationName).build();

        // When
        final Repository repo1 =
                Repository.builder().name(faker.pokemon().name() + "1").vcsOrganizationName(organizationName).build();
        final Repository repo2 =
                Repository.builder().name(faker.pokemon().name() + "2").vcsOrganizationName(organizationName).build();
        when(deliveryQuery.readRepositoriesForOrganization(organization))
                .thenReturn(
                        List.of(
                                repo1,
                                repo2
                        )
                );
        final List<Repository> repositories = vcsService.collectRepositoriesForOrganization(organization);

        // Then
        assertThat(repositories).isEqualTo(List.of(repo1, repo2));
    }
}
