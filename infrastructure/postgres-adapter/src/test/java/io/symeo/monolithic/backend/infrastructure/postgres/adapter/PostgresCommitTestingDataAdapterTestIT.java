package io.symeo.monolithic.backend.infrastructure.postgres.adapter;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.CommitTestingDataRepository;
import io.symeo.monolithic.backend.job.domain.model.testing.CommitTestingData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PostgresCommitTestingDataAdapterTestIT {
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
                .testType(faker.ancient().god())
                .repositoryName(faker.ancient().hero())
                .branchName(faker.ancient().primordial())
                .commitSha(faker.ancient().titan())
                .build();

        // When
        postgresCommitTestingDataAdapter.save(commitTestingData);

        // Then
        Assertions.assertThat(commitTestingDataRepository.findAll().size()).isEqualTo(1);
    }
}
