package io.symeo.monolithic.backend.bootstrap.it.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.symeo.monolithic.backend.bootstrap.ITAuthenticationContextProvider;
import io.symeo.monolithic.backend.bootstrap.SymeoMonolithicBackendITApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ActiveProfiles({"it", "job-api"})
@AutoConfigureWebTestClient(timeout = "36000")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = SymeoMonolithicBackendITApplication.class)
@Testcontainers
@Slf4j
@DirtiesContext
@ContextConfiguration(initializers = AbstractSymeoDataCollectionAndApiIT.WireMockInitializer.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractSymeoDataCollectionAndApiIT {

    @Container
    static PostgreSQLContainer postgresSQLContainer =
            new PostgreSQLContainer<>("postgres:13")
                    .withDatabaseName("symeo-monolithic-backend");


    @DynamicPropertySource
    static void updateProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgresSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgresSQLContainer::getUsername);
    }

    @LocalServerPort
    int port;

    @Autowired
    protected WebTestClient client;

    @Autowired
    protected WireMockServer wireMockServer;

    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    ITAuthenticationContextProvider authenticationContextProvider;
    protected final static Faker FAKER = new Faker();

    protected <T> T getStubsFromClassT(final String testResourcesDir, final String fileName, final Class<T> tClass) throws IOException {
        final String dto1 = Files.readString(Paths.get("target/test-classes/" + testResourcesDir + "/" + fileName));
        return objectMapper.readValue(dto1, tClass);
    }

    protected URI getApiURI(final String path) {
        return UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port(port)
                .path(path)
                .build()
                .toUri();
    }

    protected URI getApiURI(final String path, String paramName, String paramValue) {
        return UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port(port)
                .path(path)
                .queryParam(paramName, paramValue)
                .build()
                .toUri();
    }
    protected URI getApiURI(final String path, final Map<String, String> params) {
        final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port(port)
                .path(path);
        params.forEach(uriComponentsBuilder::queryParam);
        return uriComponentsBuilder
                .build()
                .toUri();
    }

    protected static final String GITHUB_WEBHOOK_API = "/github-app/webhook";
    protected static final String DATA_PROCESSING_JOB_REST_API_GET_START_JOB_ORGANIZATION = "/job/v1/data-processing/organization";
    protected static final String DATA_PROCESSING_JOB_REST_API_GET_START_JOB_TEAM = "/job/v1/data-processing/team";
    protected static final String DATA_PROCESSING_JOB_REST_API_GET_START_JOB_ALL = "/job/v1/data-processing/all";
    protected static final String DATA_PROCESSING_TESTING_REST_API = "/sh/v1/testing";

    public static class WireMockInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            WireMockServer wireMockServer = new WireMockServer(new WireMockConfiguration().dynamicPort());
            wireMockServer.start();

            configurableApplicationContext
                    .getBeanFactory()
                    .registerSingleton("wireMockServer", wireMockServer);

            configurableApplicationContext.addApplicationListener(
                    applicationEvent -> {
                        if (applicationEvent instanceof ContextClosedEvent) {
                            wireMockServer.stop();
                        }
                    });

            TestPropertyValues.of(
                            "infrastructure.github.app.api:http://localhost:" + wireMockServer.port() + "/",
                            "application.job-api.url:http://localhost:" + wireMockServer.port() + "/")
                    .applyTo(configurableApplicationContext);
        }

    }
}
