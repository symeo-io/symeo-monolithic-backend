package fr.catlean.monolithic.backend.infrastructure.github.adapter.it;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@SpringBootApplication
@Import(GithubAdapterITConfiguration.class)
public class GithubAdapterITApplication {

    public static void main(String[] args) {
        SpringApplication.run(GithubAdapterITApplication.class, args);
    }

    @Profile("it")
    @Bean
    public WebClient webClientIT(final WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(HttpClient.newConnection().compress(true)))
                .build();
    }


}
