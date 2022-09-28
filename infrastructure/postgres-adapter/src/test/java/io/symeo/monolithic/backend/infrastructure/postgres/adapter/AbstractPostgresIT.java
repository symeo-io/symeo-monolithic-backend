package io.symeo.monolithic.backend.infrastructure.postgres.adapter;

import io.symeo.monolithic.backend.infrastructure.postgres.SetupUnitConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = SetupUnitConfiguration.class)
@Testcontainers
@DirtiesContext
public class AbstractPostgresIT {

    @Container
    static PostgreSQLContainer postgresSQLContainer =
            new PostgreSQLContainer<>("postgres:13")
                    .withDatabaseName("symeo.-monolithic-backend");


    @DynamicPropertySource
    static void updateProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgresSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgresSQLContainer::getUsername);
    }

}
