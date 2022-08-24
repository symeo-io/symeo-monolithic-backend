package io.symeo.monolithic.backend.bootstrap;

import io.symeo.monolithic.backend.bootstrap.configuration.*;
import io.symeo.monolithic.backend.infrastructure.postgres.configuration.PostgresConfiguration;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableConfigurationProperties
@Import(value = {DomainConfiguration.class, SymeoMonolithicBackendConfiguration.class, GithubConfiguration.class,
        JsonLocalStorageConfiguration.class, PostgresConfiguration.class, RestApiConfiguration.class,
        WebCorsConfig.class, Auth0SecurityConfiguration.class, JobApiConfiguration.class,
        WebSecurityPropertiesConfig.class})
@Slf4j
@EnableAsync
@AllArgsConstructor
public class SymeoMonolithicBackendApplication {


    public static void main(String[] args) {
        SpringApplication.run(SymeoMonolithicBackendApplication.class, args);
    }

}
