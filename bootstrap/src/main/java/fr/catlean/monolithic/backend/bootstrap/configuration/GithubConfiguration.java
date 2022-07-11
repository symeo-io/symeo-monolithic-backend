package fr.catlean.monolithic.backend.bootstrap.configuration;

import fr.catlean.http.cient.DefaultCatleanHttpClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.GithubAdapter;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.client.GithubHttpClient;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.properties.GithubProperties;
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
    public GithubHttpClient githubHttpClient(HttpClient httpClient, ObjectMapper objectMapper,
                                             GithubProperties githubProperties) {
        return new GithubHttpClient(new DefaultCatleanHttpClient(httpClient), objectMapper,
                githubProperties.getToken());
    }

    @Bean
    public GithubAdapter githubAdapter(
            GithubHttpClient githubHttpClient, GithubProperties githubProperties) {
        return new GithubAdapter(githubHttpClient, githubProperties);
    }
}