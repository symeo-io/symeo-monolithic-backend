package fr.catlean.delivery.processor.bootstrap.configuration;

import catlean.http.cient.DefaultCatleanHttpClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.catlean.delivery.processor.infrastructure.github.adapter.GithubAdapter;
import fr.catlean.delivery.processor.infrastructure.github.adapter.client.GithubHttpClient;
import fr.catlean.delivery.processor.infrastructure.github.adapter.properties.GithubProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

import static com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE;

@Configuration
public class GithubConfiguration {

    @Bean
    @ConfigurationProperties("github")
    public GithubProperties githubProperties() {
        return new GithubProperties();
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }


    @Bean
    public ObjectMapper objectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(AUTO_CLOSE_SOURCE, true);
        return objectMapper;
    }

    @Bean
    public GithubHttpClient githubHttpClient(HttpClient httpClient, ObjectMapper objectMapper) {
        return new GithubHttpClient(new DefaultCatleanHttpClient(httpClient), objectMapper);
    }

    @Bean
    public GithubAdapter githubAdapter(
            GithubHttpClient githubHttpClient, GithubProperties githubProperties) {
        return new GithubAdapter(githubHttpClient, githubProperties);
    }
}
