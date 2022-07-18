package catlean.monolithic.backend.github.webhook.api.adapter.it;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;


@SpringBootApplication
@Import(GithubWebhookITConfiguration.class)
public class GithubWebhookAdapterITApplication {

    public static void main(String[] args) {
        SpringApplication.run(GithubWebhookAdapterITApplication.class, args);
    }

    @Profile("it")
    @Bean
    public WebClient webClientIT(final WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(HttpClient.newConnection().compress(true)))
                .build();
    }

}
