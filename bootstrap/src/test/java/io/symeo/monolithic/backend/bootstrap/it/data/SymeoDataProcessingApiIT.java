package io.symeo.monolithic.backend.bootstrap.it.data;

import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.VcsOrganizationEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.VcsOrganizationRepository;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.SymeoDataProcessingJobApiProperties;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;

public class SymeoDataProcessingApiIT extends AbstractSymeoDataCollectionAndApiIT {

    @Autowired
    VcsOrganizationRepository vcsOrganizationRepository;
    @Autowired
    SymeoDataProcessingJobApiProperties symeoDataProcessingJobApiProperties;

    @Test
    @Order(1)
    void should_start_data_collection_jobs_for_all_organization_ids() {
        // Given
        final UUID organizationId1 = UUID.randomUUID();
        final UUID organizationId2 = UUID.randomUUID();
        final UUID organizationId3 = UUID.randomUUID();
        final List<VcsOrganizationEntity> vcsOrganizationEntities = vcsOrganizationRepository.saveAll(List.of(
                VcsOrganizationEntity.builder()
                        .organizationEntity(OrganizationEntity.builder().id(organizationId1).name(FAKER.rickAndMorty().character()).build())
                        .name(FAKER.name().firstName())
                        .vcsId(FAKER.pokemon().location())
                        .externalId(FAKER.ancient().god())
                        .build(),
                VcsOrganizationEntity.builder()
                        .organizationEntity(OrganizationEntity.builder().id(organizationId2).name(FAKER.rickAndMorty().character()).build())
                        .name(FAKER.name().firstName())
                        .vcsId(FAKER.pokemon().location())
                        .externalId(FAKER.ancient().god())
                        .build(),
                VcsOrganizationEntity.builder()
                        .organizationEntity(OrganizationEntity.builder().id(organizationId3).name(FAKER.rickAndMorty().character()).build())
                        .name(FAKER.name().firstName())
                        .vcsId(FAKER.pokemon().location())
                        .externalId(FAKER.ancient().god())
                        .build()
        ));

        // When
        client.get()
                .uri(DATA_PROCESSING_JOB_REST_API_GET_START_JOB_ALL)
                .header(symeoDataProcessingJobApiProperties.getHeaderKey(),
                        symeoDataProcessingJobApiProperties.getApiKey())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        symeoClientAdapterWireMockServer.verify(1,
                RequestPatternBuilder.newRequestPattern().withUrl(DATA_PROCESSING_JOB_REST_API_POST_START_JOB_ORGANIZATION)
                        .withHeader(symeoDataProcessingJobApiProperties.getHeaderKey(),
                                equalTo(symeoDataProcessingJobApiProperties.getApiKey()))
                        .withRequestBody(equalToJson(String.format("{\n" +
                                "  \"organization_id\" : \"%s\",\n" +
                                "  \"vcs_organization_id\" : %s\n" +
                                "}", organizationId1, vcsOrganizationEntities.get(0).getId()))
                        ));
        symeoClientAdapterWireMockServer.verify(1,
                RequestPatternBuilder.newRequestPattern().withUrl(DATA_PROCESSING_JOB_REST_API_POST_START_JOB_ORGANIZATION)
                        .withHeader(symeoDataProcessingJobApiProperties.getHeaderKey(),
                                equalTo(symeoDataProcessingJobApiProperties.getApiKey()))
                        .withRequestBody(equalToJson(String.format("{\n" +
                                "  \"organization_id\" : \"%s\",\n" +
                                "  \"vcs_organization_id\" : %s\n" +
                                "}", organizationId2, vcsOrganizationEntities.get(1).getId()))
                        ));
        symeoClientAdapterWireMockServer.verify(1,
                RequestPatternBuilder.newRequestPattern().withUrl(DATA_PROCESSING_JOB_REST_API_POST_START_JOB_ORGANIZATION)
                        .withHeader(symeoDataProcessingJobApiProperties.getHeaderKey(),
                                equalTo(symeoDataProcessingJobApiProperties.getApiKey()))
                        .withRequestBody(equalToJson(String.format("{\n" +
                                "  \"organization_id\" : \"%s\",\n" +
                                "  \"vcs_organization_id\" : %s\n" +
                                "}", organizationId3, vcsOrganizationEntities.get(2).getId()))
                        ));
    }

    @Test
    @Order(2)
    void should_return_unauthorized_response_for_wrong_api_key_for_all_data_collection_job() {
        // When
        client.get()
                .uri(DATA_PROCESSING_JOB_REST_API_GET_START_JOB_ALL)
                .header(symeoDataProcessingJobApiProperties.getHeaderKey(), FAKER.ancient().god())
                .exchange()
                // Then
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    @Order(3)
    void should_return_unauthorized_response_for_wrong_api_key_for_job_given_an_organization() {
        // When
        client.post()
                .uri(DATA_PROCESSING_JOB_REST_API_POST_START_JOB_ORGANIZATION)
                .header(symeoDataProcessingJobApiProperties.getHeaderKey(), FAKER.gameOfThrones().character())
                .exchange()
                // Then
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    @Order(4)
    void should_return_unauthorized_response_for_wrong_api_key_for_job_given_a_team() {
        // When
        client.post()
                .uri(DATA_PROCESSING_JOB_REST_API_POST_START_JOB_TEAM)
                .header(symeoDataProcessingJobApiProperties.getHeaderKey(), FAKER.pokemon().name())
                .exchange()
                // Then
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    @Order(5)
    void should_return_unauthorized_response_for_wrong_api_key_for_job_given_repositories() {
        // When
        client.post()
                .uri(DATA_PROCESSING_JOB_REST_API_POST_START_JOB_REPOSITORIES)
                .header(symeoDataProcessingJobApiProperties.getHeaderKey(), FAKER.pokemon().name())
                .exchange()
                // Then
                .expectStatus()
                .is4xxClientError();
    }


}
