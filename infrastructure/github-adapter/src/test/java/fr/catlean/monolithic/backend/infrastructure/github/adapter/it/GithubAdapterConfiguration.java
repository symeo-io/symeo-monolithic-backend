package fr.catlean.monolithic.backend.infrastructure.github.adapter.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.GithubAdapter;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.client.GithubHttpClient;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.jwt.GithubJwtTokenProvider;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.properties.GithubProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.net.http.HttpClient;

public class GithubAdapterConfiguration {


    @Bean
    public GithubAdapter githubAdapter(final GithubHttpClient githubHttpClient,
                                       final GithubProperties githubProperties,
                                       final ObjectMapper objectMapper) {
        return new GithubAdapter(githubHttpClient, githubProperties, objectMapper);
    }

    @Bean
    @ConfigurationProperties(prefix = "github")
    public GithubProperties githubProperties() {
        return new GithubProperties();
    }

    @Bean
    public GithubHttpClient githubHttpClient(final GithubProperties githubProperties, final ObjectMapper objectMapper
            , final HttpClient httpClient, final GithubJwtTokenProvider githubJwtTokenProvider) {
        return new GithubHttpClient(objectMapper, httpClient, githubJwtTokenProvider, githubProperties.getApi());
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder().build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public GithubJwtTokenProvider githubJwtTokenProvider() {
        return () -> "fake-token";
    }

}
