package io.symeo.monolithic.backend.infrastructure.symeo.job.api.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@ActiveProfiles({"it"})
@SpringBootTest(webEnvironment = DEFINED_PORT, classes = SymeoDataProcessingApiClientITApplication.class)
@ContextConfiguration(initializers = AbstractSymeoDataProcessingApiClientAdapterIT.WireMockInitializer.class)
@EnableConfigurationProperties
public class AbstractSymeoDataProcessingApiClientAdapterIT {

    @Autowired
    protected WireMockServer wireMockServer;
    @Autowired
    protected ObjectMapper objectMapper;
    protected final static Faker faker = new Faker();


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
                            "application.job-api.url:http://localhost:" + wireMockServer.port() + "/")
                    .applyTo(configurableApplicationContext);
        }
    }

}
