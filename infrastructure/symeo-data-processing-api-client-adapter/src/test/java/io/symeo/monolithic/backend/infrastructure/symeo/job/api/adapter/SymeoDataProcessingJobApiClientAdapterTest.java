package io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.dto.PostStartDataProcessingJobForOrganizationDTO;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.dto.PostStartDataProcessingJobForRepositoriesDTO;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.dto.PostStartDataProcessingJobForTeamDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

public class SymeoDataProcessingJobApiClientAdapterTest {
    private static final Faker faker = new Faker();

    @Test
    void should_start_vcs_data_collection_given_an_organization_id_and_a_team_id_and_some_repository_ids() throws SymeoException {
        // Given
        final SymeoHttpClient symeoHttpClient = mock(SymeoHttpClient.class);
        final SymeoDataProcessingJobApiClientAdapter symeoDataProcessingJobApiClientAdapter =
                new SymeoDataProcessingJobApiClientAdapter(
                        symeoHttpClient
                );
        final UUID organizationId = UUID.randomUUID();
        final UUID teamId = UUID.randomUUID();
        final List<String> repositoryIds = List.of(faker.ancient().god(), faker.ancient().hero());

        final String deployDetectionType = faker.rickAndMorty().character();
        final String pullRequestMergedOnBranchRegex = faker.name().name();
        final String tagRegex = faker.gameOfThrones().character();
        final List<String> excludedBranchRegex = List.of("main", "staging");

        // When
        symeoDataProcessingJobApiClientAdapter.startDataProcessingJobForOrganizationIdAndTeamIdAndRepositoryIds(
                organizationId,
                teamId,
                repositoryIds,
                deployDetectionType,
                pullRequestMergedOnBranchRegex,
                tagRegex,
                excludedBranchRegex
        );

        // Then
        final ArgumentCaptor<PostStartDataProcessingJobForTeamDTO> argumentCaptor =
                ArgumentCaptor.forClass(PostStartDataProcessingJobForTeamDTO.class);
        verify(symeoHttpClient, times(1))
                .startDataProcessingJobForOrganizationIdAndTeamIdAndRepositoryIds(argumentCaptor.capture());
        final PostStartDataProcessingJobForTeamDTO value = argumentCaptor.getValue();
        assertThat(value.getRepositoryIds()).isEqualTo(repositoryIds);
        assertThat(value.getOrganizationId()).isEqualTo(organizationId);
        assertThat(value.getTeamId()).isEqualTo(teamId);
    }


    @Test
    void should_start_vcs_data_collection_given_an_organization_id_and_some_repository_ids() throws SymeoException {
        // Given
        final SymeoHttpClient symeoHttpClient = mock(SymeoHttpClient.class);
        final SymeoDataProcessingJobApiClientAdapter symeoDataProcessingJobApiClientAdapter =
                new SymeoDataProcessingJobApiClientAdapter(
                        symeoHttpClient
                );
        final UUID organizationId = UUID.randomUUID();
        final List<String> repositoryIds = List.of(faker.ancient().god(), faker.ancient().hero());
        final String deployDetectionType = faker.name().name();
        final String pullRequestMergedOnBranchRegex = faker.rickAndMorty().character();
        final String tagRegex = faker.pokemon().name();
        final List<String> excludeBranchRegexes = List.of(faker.gameOfThrones().quote());

        // When
        symeoDataProcessingJobApiClientAdapter.autoStartDataProcessingJobForOrganizationIdAndRepositoryIds(
                organizationId,
                repositoryIds,
                deployDetectionType,
                pullRequestMergedOnBranchRegex,
                tagRegex,
                excludeBranchRegexes
        );

        // Then
        final ArgumentCaptor<PostStartDataProcessingJobForRepositoriesDTO> argumentCaptor =
                ArgumentCaptor.forClass(PostStartDataProcessingJobForRepositoriesDTO.class);
        verify(symeoHttpClient, times(1))
                .startDataProcessingJobForOrganizationIdAndRepositoryIds(argumentCaptor.capture());
        final PostStartDataProcessingJobForRepositoriesDTO value = argumentCaptor.getValue();
        assertThat(value.getRepositoryIds()).isEqualTo(repositoryIds);
        assertThat(value.getOrganizationId()).isEqualTo(organizationId);
        assertThat(value.getTagRegex()).isEqualTo(tagRegex);
        assertThat(value.getPullRequestMergedOnBranchRegex()).isEqualTo(pullRequestMergedOnBranchRegex);
        assertThat(value.getDeployDetectionType()).isEqualTo(deployDetectionType);
        assertThat(value.getExcludeBranchRegexes()).isEqualTo(excludeBranchRegexes);
    }

    @Test
    void should_start_vcs_data_collection_given_an_organization_id_and_a_vcs_organization_id() throws SymeoException {
        // Given
        final SymeoHttpClient symeoHttpClient = mock(SymeoHttpClient.class);
        final SymeoDataProcessingJobApiClientAdapter symeoDataProcessingJobApiClientAdapter =
                new SymeoDataProcessingJobApiClientAdapter(
                        symeoHttpClient
                );
        final UUID organizationId = UUID.randomUUID();
        final long vcsOrganizationId = faker.number().randomNumber();

        // When
        symeoDataProcessingJobApiClientAdapter.startDataProcessingJobForOrganizationIdAndVcsOrganizationId(
                organizationId,
                vcsOrganizationId
        );

        // Then
        final ArgumentCaptor<PostStartDataProcessingJobForOrganizationDTO> argumentCaptor =
                ArgumentCaptor.forClass(PostStartDataProcessingJobForOrganizationDTO.class);
        verify(symeoHttpClient, times(1))
                .startDataProcessingJobForOrganizationIdAndVcsOrganizationId(argumentCaptor.capture());
        final PostStartDataProcessingJobForOrganizationDTO value = argumentCaptor.getValue();
        assertThat(value.getVcsOrganizationId()).isEqualTo(vcsOrganizationId);
        assertThat(value.getOrganizationId()).isEqualTo(organizationId);
    }


}
