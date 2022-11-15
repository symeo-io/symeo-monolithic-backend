package io.symeo.monolithic.backend.bootstrap.it.data;

import io.symeo.monolithic.backend.data.processing.contract.api.model.CollectTestingDataRequestContract;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationApiKeyEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationApiKeyRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.CommitTestingDataRepository;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.SymeoDataProcessingJobApiProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class SymeoTestingRestApiIT extends AbstractSymeoDataCollectionAndApiIT {
    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    private CommitTestingDataRepository commitTestingDataRepository;
    @Autowired
    private OrganizationApiKeyRepository organizationApiKeyRepository;

    @AfterEach
    void tearDown() {
        organizationRepository.deleteAll();
        commitTestingDataRepository.deleteAll();
        organizationApiKeyRepository.deleteAll();
    }

    @Test
    void should_fail_for_unknown_api_key() throws IOException {
        // Given
        final OrganizationEntity organizationEntity =
                OrganizationEntity.builder().id(UUID.randomUUID()).name(FAKER.rickAndMorty().character()).build();
        organizationRepository.save(organizationEntity);

        final byte[] bytes = Files.readString(Paths.get("target/test-classes/testing/post_testing.json")).getBytes();

        // When
        client.post()
                .uri(DATA_PROCESSING_TESTING_REST_API)
                .bodyValue(bytes)
                .exchange()
                // Then
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    void should_success_for_known_api_key() throws IOException {
        // Given
        final String key = UUID.randomUUID().toString();
        final OrganizationEntity organizationEntity = OrganizationEntity.builder()
                .id(UUID.randomUUID())
                .name(FAKER.rickAndMorty().character())
                .build();
        organizationRepository.save(organizationEntity);
        final OrganizationApiKeyEntity organizationApiKeyEntity = OrganizationApiKeyEntity.builder()
                .id(UUID.randomUUID())
                .key(key)
                .name(FAKER.rickAndMorty().character())
                .organizationId(organizationEntity.getId())
                .build();
        organizationApiKeyRepository.save(organizationApiKeyEntity);


        final CollectTestingDataRequestContract collectTestingDataRequestContract =
                new ObjectMapper().readValue(Paths.get("target/test-classes/testing/post_testing.json").toFile(),
                        CollectTestingDataRequestContract.class);

        // When
        client.post()
                .uri(DATA_PROCESSING_TESTING_REST_API)
                .header("X-API-KEY", key)
                .body(Mono.just(collectTestingDataRequestContract), CollectTestingDataRequestContract.class)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        assertThat(commitTestingDataRepository.findAll()).hasSize(1);
    }
}
