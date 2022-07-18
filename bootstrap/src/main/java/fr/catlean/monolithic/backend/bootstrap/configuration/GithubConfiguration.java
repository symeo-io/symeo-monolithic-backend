package fr.catlean.monolithic.backend.bootstrap.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.GithubAdapter;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.client.GithubHttpClient;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.jwt.DefaultGithubJwtTokenProvider;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.jwt.GithubJwtTokenProvider;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.properties.GithubProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

import static com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE;

@Configuration
public class GithubConfiguration {

    @Bean
    @ConfigurationProperties("github.app")
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
    public GithubHttpClient githubHttpClient(final HttpClient httpClient,
                                             final ObjectMapper objectMapper,
                                             final GithubJwtTokenProvider githubJwtTokenProvider,
                                             final GithubProperties githubProperties) {
        return new GithubHttpClient(objectMapper, httpClient, githubJwtTokenProvider, githubProperties.getApi());
    }

    @Bean
    public GithubAdapter githubAdapter(
            final GithubHttpClient githubHttpClient, final GithubProperties githubProperties,
            final ObjectMapper objectMapper) {
        return new GithubAdapter(githubHttpClient, githubProperties, objectMapper);
    }

    @Bean
    public GithubJwtTokenProvider githubJwtTokenProvider(final GithubProperties githubProperties) {
        return new DefaultGithubJwtTokenProvider(githubProperties.getPrivateKeyCertificatePath(),
                githubProperties.getGithubAppId());
    }
}
