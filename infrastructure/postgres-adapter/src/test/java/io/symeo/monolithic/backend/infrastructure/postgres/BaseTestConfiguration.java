package io.symeo.monolithic.backend.infrastructure.postgres;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EntityScan(basePackages = {
    "io.symeo.monolithic.backend.infrastructure.postgres.entity"
})
@EnableJpaRepositories(basePackages = {
    "io.symeo.monolithic.backend.infrastructure.postgres.repository"
})
@EnableTransactionManagement
public class BaseTestConfiguration {

}
