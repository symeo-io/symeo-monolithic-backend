package fr.catlean.delivery.processor.infrastructure.postgres.it.adapter;

import com.github.javafaker.Faker;
import fr.catlean.delivery.processor.domain.model.PullRequest;
import fr.catlean.delivery.processor.infrastructure.postgres.PostgresAdapter;
import fr.catlean.delivery.processor.infrastructure.postgres.entity.PullRequestEntity;
import fr.catlean.delivery.processor.infrastructure.postgres.it.SetupConfiguration;
import fr.catlean.delivery.processor.infrastructure.postgres.repository.PullRequestRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = SetupConfiguration.class)
public class PostgresAdapterTestIT {

    private final Faker faker = new Faker();

    @Autowired
    private PullRequestRepository pullRequestRepository;


    @Test
    void should_save_pull_requests_to_postgres() {
        // Given
        final PostgresAdapter postgresAdapter = new PostgresAdapter(pullRequestRepository);
        final List<PullRequest> pullRequestsToSave = List.of(
                buildPullRequest(1),
                buildPullRequest(2),
                buildPullRequest(3)
        );

        // When
        postgresAdapter.savePullRequestDetails(pullRequestsToSave);

        // Then
        final List<PullRequestEntity> all = pullRequestRepository.findAll();
        Assertions.assertThat(all).hasSize(pullRequestsToSave.size());
    }


    private PullRequest buildPullRequest(int id) {
        return PullRequest.builder()
                .id(id)
                .number(id)
                .title(faker.name().title())
                .lastUpdateDate(faker.date().past(1, TimeUnit.DAYS))
                .creationDate(faker.date().past(7, TimeUnit.DAYS))
                .mergeDate(new Date())
                .vcsUrl(faker.pokemon().name())
                .state(faker.pokemon().location())
                .deletedLineNumber(faker.number().numberBetween(0, 20000))
                .authorLogin(faker.name().firstName())
                .commitNumber(faker.number().randomDigit())
                .addedLineNumber(faker.number().numberBetween(0, 20000))
                .isDraft(true)
                .isMerged(false)
                .build();
    }
}
