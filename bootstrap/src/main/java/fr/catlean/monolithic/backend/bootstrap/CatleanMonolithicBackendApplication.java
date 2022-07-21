package fr.catlean.monolithic.backend.bootstrap;

import fr.catlean.monolithic.backend.bootstrap.configuration.*;
import fr.catlean.monolithic.backend.infrastructure.postgres.configuration.PostgresConfiguration;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableConfigurationProperties
@Import(value = {DomainConfiguration.class, CatleanDeliveryProcessorConfiguration.class, GithubConfiguration.class,
        JsonLocalStorageConfiguration.class, PostgresConfiguration.class, RestApiConfiguration.class,
        WebCorsConfig.class, SentryConfiguration.class, Auth0SecurityConfiguration.class,
        WebCorsPropertiesConfig.class})
@Slf4j
@AllArgsConstructor
public class CatleanMonolithicBackendApplication {


    public static void main(String[] args) {
        SpringApplication.run(CatleanMonolithicBackendApplication.class, args);
    }

}
