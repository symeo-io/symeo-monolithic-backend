package fr.catlean.monolithic.backend.infrastructure.postgres.it;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EntityScan(basePackages = {
    "fr.catlean.monolithic.backend.infrastructure.postgres.entity"
})
@EnableJpaRepositories(basePackages = {
    "fr.catlean.monolithic.backend.infrastructure.postgres.repository"
})
@EnableTransactionManagement
public class BaseTestConfiguration {

}
