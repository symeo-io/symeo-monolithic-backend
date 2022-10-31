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

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ActiveProfiles({"it"})
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = SymeoDataProcessingApiClientITApplication.class)
@ContextConfiguration(initializers = AbstractSymeoDataProcessingApiClientAdapterIT.WireMockInitializer.class)
@EnableConfigurationProperties
public class AbstractSymeoDataProcessingApiClientAdapterIT {

    @Autowired
    protected WireMockServer dataProcessingWireMockServer;
    @Autowired
    protected ObjectMapper objectMapper;
    protected final static Faker faker = new Faker();


    public static class WireMockInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            WireMockServer dataProcessingWireMockServer = new WireMockServer(new WireMockConfiguration().dynamicPort());
            dataProcessingWireMockServer.start();

            configurableApplicationContext
                    .getBeanFactory()
                    .registerSingleton("dataProcessingWireMockServer", dataProcessingWireMockServer);

            configurableApplicationContext.addApplicationListener(
                    applicationEvent -> {
                        if (applicationEvent instanceof ContextClosedEvent) {
                            dataProcessingWireMockServer.stop();
                        }
                    });

            TestPropertyValues.of(
                            "application.job-api.url:http://localhost:" + dataProcessingWireMockServer.port() + "/")
                    .applyTo(configurableApplicationContext);
        }
    }

}
