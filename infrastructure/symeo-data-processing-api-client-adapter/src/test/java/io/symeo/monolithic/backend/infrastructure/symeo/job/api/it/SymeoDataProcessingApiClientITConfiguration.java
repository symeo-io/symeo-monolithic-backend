package io.symeo.monolithic.backend.infrastructure.symeo.job.api.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.SymeoDataProcessingJobApiClientAdapter;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.SymeoDataProcessingJobApiProperties;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.SymeoHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.net.http.HttpClient;

public class SymeoDataProcessingApiClientITConfiguration {


    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder().build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @ConfigurationProperties("application.job-api")
    public SymeoDataProcessingJobApiProperties symeoJobApiProperties() {
        return new SymeoDataProcessingJobApiProperties();
    }

    @Bean
    public SymeoHttpClient symeoHttpClient(final HttpClient httpClient,
                                           final SymeoDataProcessingJobApiProperties symeoDataProcessingJobApiProperties) {
        return new SymeoHttpClient(httpClient, symeoDataProcessingJobApiProperties);
    }

    @Bean
    public SymeoDataProcessingJobApiClientAdapter symeoDataProcessingJobApiClientAdapter(final SymeoHttpClient symeoHttpClient) {
        return new SymeoDataProcessingJobApiClientAdapter(symeoHttpClient);

    }
}
