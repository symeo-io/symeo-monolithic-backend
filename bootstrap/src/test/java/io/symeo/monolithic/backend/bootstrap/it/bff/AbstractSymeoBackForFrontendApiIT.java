package io.symeo.monolithic.backend.bootstrap.it.bff;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.bootstrap.ITAuthenticationContextProvider;
import io.symeo.monolithic.backend.bootstrap.SymeoMonolithicBackendITApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.util.Map;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ActiveProfiles({"it", "front-api"})
@AutoConfigureWebTestClient(timeout = "36000")
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = SymeoMonolithicBackendITApplication.class)
@Testcontainers
@Slf4j
@DirtiesContext
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractSymeoBackForFrontendApiIT {

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

    @LocalServerPort
    int port;

    @Autowired
    WebTestClient client;


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

    protected static final String USER_REST_API_GET_ME = "/api/v1/me";
    protected static final String USER_REST_API_POST_ME_ORGANIZATION = "/api/v1/me/organization";
    protected static final String USER_REST_API_POST_ME_ONBOARDING = "/api/v1/me/onboarding";
    protected static final String TEAM_REST_API = "/api/v1/teams";
    protected static final String REPOSITORIES_REST_API_GET = "/api/v1/repositories";
    protected static final String ORGANIZATIONS_REST_API_USERS = "/api/v1/organizations/users";
    protected static final String ORGANIZATION_REST_API_SETTINGS = "/api/v1/organization/settings";
    protected static final String TEAMS_GOALS_REST_API_TIME_TO_MERGE_HISTOGRAM = "/api/v1/teams/goals/time-to-merge" +
            "/histogram";
    protected static final String TEAMS_GOALS_REST_API_TIME_TO_MERGE_CURVES = "/api/v1/teams/goals/time-to-merge" +
            "/curves";
    protected static final String TEAMS_GOALS_REST_API_TIME_TO_MERGE_METRICS = "/api/v1/teams/goals/time-to-merge" +
            "/metrics";
    protected static final String TEAMS_GOALS_REST_API_PULL_REQUEST_SIZE_HISTOGRAM = "/api/v1/teams/goals/pull" +
            "-request-size/histogram";
    protected static final String TEAMS_GOALS_REST_API_PULL_REQUEST_SIZE_METRICS = "/api/v1/teams/goals/pull" +
            "-request-size/metrics";
    protected static final String TEAMS_GOALS_REST_API_PULL_REQUEST_SIZE_CURVES = "/api/v1/teams/goals/pull-request" +
            "-size/curves";
    protected static final String TEAMS_REST_API_PULL_REQUESTS = "/api/v1/teams/pull-requests";
    protected static final String TEAMS_GOALS_REST_API = "/api/v1/teams/goals";
    protected static final String JOBS_REST_API_STATUS = "/api/v1/jobs/status";

    @Autowired
    ITAuthenticationContextProvider authenticationContextProvider;

    static protected final Faker faker = new Faker();

}
