package io.symeo.monolithic.backend.infrastructure.postgres;

import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.CustomCommitRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.CustomCycleTimeRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.CustomPullRequestViewRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.CustomPullRequestWithCommitsAndCommentsRepository;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;

@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = {
        "io.symeo.monolithic.backend.infrastructure.postgres.entity"
})
@EnableJpaRepositories(basePackages = {
        "io.symeo.monolithic.backend.infrastructure.postgres.repository"
})
@EnableTransactionManagement
@EnableJpaAuditing
public class SetupUnitConfiguration {

    @Bean
    public CustomPullRequestViewRepository customPullRequestViewRepository(final EntityManager entityManager) {
        return new CustomPullRequestViewRepository(entityManager);
    }


    @Bean
    public CustomCommitRepository customCommitRepository(final EntityManager entityManager) {
        return new CustomCommitRepository(entityManager);
    }


    @Bean
    public CustomPullRequestWithCommitsAndCommentsRepository customPullRequestWithCommitsAndCommentsRepository(final EntityManager entityManager) {
        return new CustomPullRequestWithCommitsAndCommentsRepository(entityManager);
    }

    @Bean
    public CustomCycleTimeRepository customCycleTimeRepository(final EntityManager entityManager) {
        return new CustomCycleTimeRepository(entityManager);
    }
}
