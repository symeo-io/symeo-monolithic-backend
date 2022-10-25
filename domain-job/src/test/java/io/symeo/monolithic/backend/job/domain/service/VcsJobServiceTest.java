package io.symeo.monolithic.backend.job.domain.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.job.domain.adapter.GithubAdapter;
import io.symeo.monolithic.backend.job.domain.model.Repository;
import io.symeo.monolithic.backend.job.domain.model.VcsOrganization;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.when;

public class VcsJobServiceTest {

    private static final Faker faker = new Faker();

    @Test
    void should_collect_all_repositories_given_a_vcs_organization() {
        // Given
        final VcsJobService vcsJobService = new VcsJobService();
        final VcsOrganization vcsOrganization = VcsOrganization.builder().build();
        final GithubAdapter githubAdapter = new GithubAdapter();

        // When
//        when(githubAdapter.getRepositoriesForVcsOrganizationName(vcsOrganization.getName()))
//                .thenReturn(List.of(Repository.builder().name(faker.address().buildingNumber()).build()));
        vcsJobService.collectRepositoriesForVcsOrganization(vcsOrganization);

        // Then
    }
}
