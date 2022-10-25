package io.symeo.monolithic.backend.bootstrap.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.symeo.monolithic.backend.infrastructure.github.adapter.GithubAdapter;
import io.symeo.monolithic.backend.infrastructure.github.adapter.GithubHttpApiClient;
import io.symeo.monolithic.backend.infrastructure.github.adapter.jwt.DefaultGithubJwtTokenProvider;
import io.symeo.monolithic.backend.infrastructure.github.adapter.jwt.GithubJwtTokenProvider;
import io.symeo.monolithic.backend.job.domain.github.properties.GithubProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

import static com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE;

@Configuration
public class GithubConfiguration {

    @Bean
    @ConfigurationProperties("infrastructure.github.app")
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
    public GithubHttpApiClient githubHttpClient(final HttpClient httpClient,
                                                final ObjectMapper objectMapper,
                                                final GithubJwtTokenProvider githubJwtTokenProvider,
                                                final GithubProperties githubProperties) {
        return new GithubHttpApiClient(objectMapper, httpClient, githubJwtTokenProvider, githubProperties.getApi());
    }

    @Bean
    public GithubAdapter githubAdapter(
            final GithubHttpApiClient githubHttpApiClient, final GithubProperties githubProperties,
            final ObjectMapper objectMapper) {
        return new GithubAdapter(githubHttpApiClient, githubProperties, objectMapper);
    }

    @Bean
    public GithubJwtTokenProvider githubJwtTokenProvider(final GithubProperties githubProperties) {
        return new DefaultGithubJwtTokenProvider(githubProperties.getPrivateKeyCertificatePath(),
                githubProperties.getGithubAppId());
    }
}
