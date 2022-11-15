package io.symeo.monolithic.backend.infrastructure.postgres.adapter;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.CommitTestingDataRepository;
import io.symeo.monolithic.backend.job.domain.model.testing.CommitTestingData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

public class PostgresCommitTestingDataAdapterTestIT extends AbstractPostgresIT {
    private final Faker faker = new Faker();
    @Autowired
    private CommitTestingDataRepository commitTestingDataRepository;

    @AfterEach
    void tearDown() {
        commitTestingDataRepository.deleteAll();
    }

    @Test
    void should_save() throws SymeoException {
        // Given
        final PostgresCommitTestingDataAdapter postgresCommitTestingDataAdapter =
                new PostgresCommitTestingDataAdapter(commitTestingDataRepository);
        CommitTestingData commitTestingData = CommitTestingData.builder()
                .repositoryName(faker.ancient().hero())
                .branchName(faker.ancient().primordial())
                .commitSha(faker.ancient().titan())
                .organizationId(UUID.randomUUID())
                .date(new Date())
                .build();

        // When
        postgresCommitTestingDataAdapter.save(commitTestingData);

        // Then
        Assertions.assertThat(commitTestingDataRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    void should_return_false_when_no_data_for_org_and_repo() throws SymeoException {
        // Given
        final PostgresCommitTestingDataAdapter postgresCommitTestingDataAdapter =
                new PostgresCommitTestingDataAdapter(commitTestingDataRepository);

        Boolean hasData = postgresCommitTestingDataAdapter.hasDataForOrganizationAndRepositories(UUID.randomUUID(), Arrays.asList(faker.ancient().hero(), faker.ancient().hero()));

        // Then
        Assertions.assertThat(hasData).isEqualTo(false);
    }

    @Test
    void should_return_true_when_data_for_org_and_repo() throws SymeoException {
        // Given
        final PostgresCommitTestingDataAdapter postgresCommitTestingDataAdapter =
                new PostgresCommitTestingDataAdapter(commitTestingDataRepository);
        CommitTestingData commitTestingData = CommitTestingData.builder()
                .repositoryName(faker.ancient().hero())
                .branchName(faker.ancient().primordial())
                .commitSha(faker.ancient().titan())
                .organizationId(UUID.randomUUID())
                .date(new Date())
                .build();

        postgresCommitTestingDataAdapter.save(commitTestingData);

        // When
        Boolean hasData = postgresCommitTestingDataAdapter.hasDataForOrganizationAndRepositories(commitTestingData.getOrganizationId(), Collections.singletonList(commitTestingData.getRepositoryName()));

        // Then
        Assertions.assertThat(hasData).isEqualTo(true);
    }
}
