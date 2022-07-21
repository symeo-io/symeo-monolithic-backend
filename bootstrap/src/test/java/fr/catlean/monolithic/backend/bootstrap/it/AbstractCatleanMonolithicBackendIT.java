package fr.catlean.monolithic.backend.bootstrap.it;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.bootstrap.CatleanMonolithicBackendITApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ActiveProfiles({"it"})
@AutoConfigureWebTestClient(timeout = "36000")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = CatleanMonolithicBackendITApplication.class)
@Testcontainers
@Slf4j
public abstract class AbstractCatleanMonolithicBackendIT {

    @Container
    static PostgreSQLContainer postgresSQLContainer =
            new PostgreSQLContainer<>("postgres:13")
                    .withDatabaseName("catlean-monolithic-backend");


    @DynamicPropertySource
    static void updateProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgresSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgresSQLContainer::getUsername);
    }

    @LocalServerPort
    int port;

    @Autowired
    WebTestClient client;
    protected final Faker faker = new Faker();

    protected URI getApiURI(final String path, final String... contextPath) {
        return UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port(port)
                .path(path)
                .pathSegment(contextPath)
                .build()
                .toUri();
    }


}
