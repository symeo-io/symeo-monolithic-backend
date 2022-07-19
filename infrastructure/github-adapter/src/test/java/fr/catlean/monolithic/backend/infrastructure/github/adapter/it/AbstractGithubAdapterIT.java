package fr.catlean.monolithic.backend.infrastructure.github.adapter.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@ActiveProfiles({"it"})
@AutoConfigureWebTestClient(timeout = "36000")
@SpringBootTest(webEnvironment = DEFINED_PORT, classes = GithubAdapterITApplication.class)
@ContextConfiguration(initializers = AbstractGithubAdapterIT.WireMockInitializer.class)
@EnableConfigurationProperties
public class AbstractGithubAdapterIT {

    @Autowired
    protected WebTestClient client;

    @Autowired
    protected WireMockServer wireMockServer;

    @Autowired
    protected ObjectMapper objectMapper;
    protected final static Faker FAKER = new Faker();


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
                            "github.app.api:http://localhost:" + wireMockServer.port() + "/")
                    .applyTo(configurableApplicationContext);
        }
    }

    protected <T> T getStubsFromClassT(final String testResourcesDir, final String fileName, final Class<T> tClass) throws IOException {
        final String dto1 = Files.readString(Paths.get("target/test-classes/" + testResourcesDir + "/" + fileName));
        return objectMapper.readValue(dto1, tClass);
    }
}
