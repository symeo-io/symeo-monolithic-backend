package io.symeo.monolithic.backend.infrastructure.github.adapter.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.github.adapter.GithubHttpApiClient;
import io.symeo.monolithic.backend.infrastructure.github.adapter.jwt.GithubJwtTokenProvider;
import io.symeo.monolithic.backend.job.domain.github.properties.GithubProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.net.http.HttpClient;

public class GithubAdapterITConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "github.app")
    public GithubProperties githubProperties() {
        return new GithubProperties();
    }

    @Bean
    public GithubHttpApiClient githubHttpClient(final GithubProperties githubProperties, final ObjectMapper objectMapper
            , final HttpClient httpClient, final GithubJwtTokenProvider githubJwtTokenProvider) {
        return new GithubHttpApiClient(objectMapper, httpClient, githubJwtTokenProvider, githubProperties.getApi());
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
    public GithubJwtTokenProviderMock githubJwtTokenProvider() {
        return new GithubJwtTokenProviderMock();
    }

    @Data
    public static class GithubJwtTokenProviderMock implements GithubJwtTokenProvider {

        private String signedJwtToken;
        private int count = 0;

        @Override
        public String generateSignedJwtToken() throws SymeoException {
            count++;
            return signedJwtToken;
        }
    }
}
