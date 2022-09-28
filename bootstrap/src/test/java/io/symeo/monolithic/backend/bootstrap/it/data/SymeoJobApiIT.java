package io.symeo.monolithic.backend.bootstrap.it.data;

import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.SymeoJobApiProperties;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;

public class SymeoJobApiIT extends AbstractSymeoDataCollectionAndApiIT {

    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    SymeoJobApiProperties symeoJobApiProperties;

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
                .header(symeoJobApiProperties.getHeaderKey(), symeoJobApiProperties.getApiKey())
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        wireMockServer.verify(1,
                RequestPatternBuilder.newRequestPattern().withUrl(String.format(DATA_PROCESSING_JOB_REST_API_GET_START_JOB_ORGANIZATION +
                                "?organization_id=%s", organizationEntities.get(0).getId().toString()))
                        .withHeader(symeoJobApiProperties.getHeaderKey(), equalTo(symeoJobApiProperties.getApiKey())));
        wireMockServer.verify(1,
                RequestPatternBuilder.newRequestPattern().withUrl(String.format(DATA_PROCESSING_JOB_REST_API_GET_START_JOB_ORGANIZATION +
                                "?organization_id=%s", organizationEntities.get(1).getId().toString()))
                        .withHeader(symeoJobApiProperties.getHeaderKey(), equalTo(symeoJobApiProperties.getApiKey())));
        wireMockServer.verify(1,
                RequestPatternBuilder.newRequestPattern().withUrl(String.format(DATA_PROCESSING_JOB_REST_API_GET_START_JOB_ORGANIZATION +
                                "?organization_id=%s", organizationEntities.get(2).getId().toString()))
                        .withHeader(symeoJobApiProperties.getHeaderKey(), equalTo(symeoJobApiProperties.getApiKey())));
    }

    @Test
    @Order(2)
    void should_return_unauthorized_response_for_wrong_api_key() {
        // When
        client.get()
                .uri(DATA_PROCESSING_JOB_REST_API_GET_START_JOB_ALL)
                .header(symeoJobApiProperties.getHeaderKey(), FAKER.ancient().god())
                .exchange()
                // Then
                .expectStatus()
                .is4xxClientError();
    }
}
