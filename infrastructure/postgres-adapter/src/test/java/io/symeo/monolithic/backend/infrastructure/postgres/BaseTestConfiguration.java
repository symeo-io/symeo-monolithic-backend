package io.symeo.monolithic.backend.infrastructure.postgres;

import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.CustomPullRequestViewRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestWithCommitsAndCommentsRepository;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;

@Configuration
@EntityScan(basePackages = {
        "io.symeo.monolithic.backend.infrastructure.postgres.entity"
})
@EnableJpaRepositories(basePackages = {
        "io.symeo.monolithic.backend.infrastructure.postgres.repository"
})
@EnableTransactionManagement
public class BaseTestConfiguration {

    @Bean
    public CustomPullRequestViewRepository customPullRequestViewRepository(final EntityManager entityManager) {
        return new CustomPullRequestViewRepository(entityManager);
    }

}
