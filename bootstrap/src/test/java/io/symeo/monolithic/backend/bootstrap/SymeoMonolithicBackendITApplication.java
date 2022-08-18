package io.symeo.monolithic.backend.bootstrap;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import static reactor.netty.http.client.HttpClient.newConnection;

@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class}
)
@EnableConfigurationProperties
@Slf4j
@AllArgsConstructor
public class SymeoMonolithicBackendITApplication {

    public static void main(String[] args) {
        SpringApplication.run(SymeoMonolithicBackendITApplication.class, args);
    }

    @Bean
    public WebClient webClientIT(final WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(newConnection().compress(true)))
                .build();
    }

    @Bean
    @Primary
    public ITAuthenticationContextProvider authenticationContextProvider() {
        return new ITAuthenticationContextProvider();
    }

    @Bean
    @Primary
    public ITGithubJwtTokenProvider itGithubJwtTokenProvider() {
        return new ITGithubJwtTokenProvider();
    }
}
