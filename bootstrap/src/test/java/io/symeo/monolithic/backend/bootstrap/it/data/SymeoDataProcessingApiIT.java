package io.symeo.monolithic.backend.bootstrap.it.data;

import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.SymeoDataProcessingJobApiProperties;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;

public class SymeoDataProcessingApiIT extends AbstractSymeoDataCollectionAndApiIT {

    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    SymeoDataProcessingJobApiProperties symeoDataProcessingJobApiProperties;

    @Test
    @Order(1)
    void should_start_data_collection_jobs_for_all_organization_ids() {
        // Given
        final List<OrganizationEntity> organizationEntities = List.of(
                OrganizationEntity.builder().id(UUID.randomUUID()).name(FAKER.rickAndMorty().character()).build(),
                OrganizationEntity.builder().id(UUID.randomUUID()).name(FAKER.rickAndMorty().character()).build(),
                OrganizationEntity.builder().id(UUID.randomUUID()).name(FAKER.rickAndMorty().character()).build()
        );
        organizationRepository.saveAll(organizationEntities);

        // When
        client.get()
                .uri(DATA_PROCESSING_JOB_REST_API_GET_START_JOB_ALL)
                .header(symeoDataProcessingJobApiProperties.getHeaderKey(), symeoDataProcessingJobApiProperties.getApiKey())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        wireMockServer.verify(1,
                RequestPatternBuilder.newRequestPattern().withUrl(String.format(DATA_PROCESSING_JOB_REST_API_GET_START_JOB_ORGANIZATION +
                                "?organization_id=%s", organizationEntities.get(0).getId().toString()))
                        .withHeader(symeoDataProcessingJobApiProperties.getHeaderKey(), equalTo(symeoDataProcessingJobApiProperties.getApiKey())));
        wireMockServer.verify(1,
                RequestPatternBuilder.newRequestPattern().withUrl(String.format(DATA_PROCESSING_JOB_REST_API_GET_START_JOB_ORGANIZATION +
                                "?organization_id=%s", organizationEntities.get(1).getId().toString()))
                        .withHeader(symeoDataProcessingJobApiProperties.getHeaderKey(), equalTo(symeoDataProcessingJobApiProperties.getApiKey())));
        wireMockServer.verify(1,
                RequestPatternBuilder.newRequestPattern().withUrl(String.format(DATA_PROCESSING_JOB_REST_API_GET_START_JOB_ORGANIZATION +
                                "?organization_id=%s", organizationEntities.get(2).getId().toString()))
                        .withHeader(symeoDataProcessingJobApiProperties.getHeaderKey(), equalTo(symeoDataProcessingJobApiProperties.getApiKey())));
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
        client.get()
                .uri(DATA_PROCESSING_JOB_REST_API_GET_START_JOB_ORGANIZATION)
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
        client.get()
                .uri(DATA_PROCESSING_JOB_REST_API_GET_START_JOB_TEAM)
                .header(symeoDataProcessingJobApiProperties.getHeaderKey(), FAKER.pokemon().name())
                .exchange()
                // Then
                .expectStatus()
                .is4xxClientError();
    }
}
