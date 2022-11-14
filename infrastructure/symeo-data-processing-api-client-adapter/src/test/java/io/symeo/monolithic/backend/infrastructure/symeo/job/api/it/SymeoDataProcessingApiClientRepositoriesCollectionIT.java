package io.symeo.monolithic.backend.infrastructure.symeo.job.api.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.SymeoDataProcessingJobApiClientAdapter;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.SymeoDataProcessingJobApiProperties;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.SymeoHttpClient;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.dto.PostStartDataProcessingJobForOrganizationDTO;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.dto.PostStartDataProcessingJobForRepositoriesDTO;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.dto.PostStartDataProcessingJobForTeamDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;

public class SymeoDataProcessingApiClientRepositoriesCollectionIT extends AbstractSymeoDataProcessingApiClientAdapterIT {

    @Autowired
    public SymeoHttpClient symeoHttpClient;
    @Autowired
    public SymeoDataProcessingJobApiProperties symeoDataProcessingJobApiProperties;
    @Autowired
    public SymeoDataProcessingJobApiClientAdapter symeoDataProcessingJobApiClientAdapter;

    @AfterEach
    void tearDown() {
        dataProcessingWireMockServer.resetAll();
    }

    @Test
    void should_start_data_processing_job_given_an_organization_id_and_a_vcs_organization_id() throws JsonProcessingException, SymeoException {
        // Given
        final PostStartDataProcessingJobForOrganizationDTO dto =
                PostStartDataProcessingJobForOrganizationDTO.builder()
                        .organizationId(UUID.randomUUID())
                        .vcsOrganizationId(faker.number().randomNumber())
                        .build();

        // When
        symeoDataProcessingJobApiClientAdapter.startDataProcessingJobForOrganizationIdAndVcsOrganizationId(
                dto.getOrganizationId(), dto.getVcsOrganizationId()
        );

        // Then
        dataProcessingWireMockServer.verify(1,
                RequestPatternBuilder.newRequestPattern().withUrl("/job/v1/data-processing/organization" +
                                "/vcs_organization")
                        .withHeader(symeoDataProcessingJobApiProperties.getHeaderKey(),
                                equalTo(symeoDataProcessingJobApiProperties.getApiKey()))
                        .withRequestBody(binaryEqualTo(objectMapper.writeValueAsBytes(dto))));
    }

    @Test
    void should_start_data_processing_job_given_an_organization_id_and_some_repository_ids() throws JsonProcessingException, SymeoException {
        // Given
        final PostStartDataProcessingJobForRepositoriesDTO dto =
                PostStartDataProcessingJobForRepositoriesDTO.builder()
                        .organizationId(UUID.randomUUID())
                        .repositoryIds(List.of(faker.name().firstName(), faker.ancient().hero()))
                        .build();


        // When
        symeoDataProcessingJobApiClientAdapter.autoStartDataProcessingJobForOrganizationIdAndRepositoryIds(
                dto.getOrganizationId(), dto.getRepositoryIds()
        );

        // Then
        dataProcessingWireMockServer.verify(1,
                RequestPatternBuilder.newRequestPattern().withUrl("/job/v1/data-processing/organization/repositories")
                        .withHeader(symeoDataProcessingJobApiProperties.getHeaderKey(),
                                equalTo(symeoDataProcessingJobApiProperties.getApiKey()))
                        .withRequestBody(binaryEqualTo(objectMapper.writeValueAsBytes(dto))));
    }

    @Test
    void should_start_data_processing_job_given_an_organization_id_and_a_team_id_and_some_repository_ids() throws JsonProcessingException, SymeoException {
        // Given
        final String deployDetectionType = faker.rickAndMorty().character();
        final String pullRequestMergedOnBranchRegex = faker.name().name();
        final String tagRegex = faker.gameOfThrones().character();
        final List<String> excludedBranchRegex = List.of("main", "staging");

        final PostStartDataProcessingJobForTeamDTO dto =
                PostStartDataProcessingJobForTeamDTO.builder()
                        .organizationId(UUID.randomUUID())
                        .teamId(UUID.randomUUID())
                        .repositoryIds(List.of(faker.name().firstName(), faker.ancient().hero()))
                        .deployDetectionType(deployDetectionType)
                        .pullRequestMergedOnBranchRegex(pullRequestMergedOnBranchRegex)
                        .tagRegex(tagRegex)
                        .excludeBranchRegexes(excludedBranchRegex)
                        .build();


        // When
        symeoDataProcessingJobApiClientAdapter.startDataProcessingJobForOrganizationIdAndTeamIdAndRepositoryIds(
                dto.getOrganizationId(), dto.getTeamId(), dto.getRepositoryIds(), dto.getDeployDetectionType(), dto.getPullRequestMergedOnBranchRegex(),
                dto.getTagRegex(), dto.getExcludeBranchRegexes()
        );

        // Then
        dataProcessingWireMockServer.verify(1,
                RequestPatternBuilder.newRequestPattern().withUrl("/job/v1/data-processing/organization/team" +
                                "/repositories")
                        .withHeader(symeoDataProcessingJobApiProperties.getHeaderKey(),
                                equalTo(symeoDataProcessingJobApiProperties.getApiKey()))
                        .withRequestBody(binaryEqualTo(objectMapper.writeValueAsBytes(dto))));
    }


}
