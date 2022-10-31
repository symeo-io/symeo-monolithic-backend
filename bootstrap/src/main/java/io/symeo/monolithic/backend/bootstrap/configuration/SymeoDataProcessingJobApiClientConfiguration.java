package io.symeo.monolithic.backend.bootstrap.configuration;

import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.SymeoDataProcessingJobApiClientAdapter;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.SymeoDataProcessingJobApiProperties;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.SymeoHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.net.http.HttpClient;

public class SymeoDataProcessingJobApiClientConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "application.job-api")
    public SymeoDataProcessingJobApiProperties symeoJobApiProperties() {
        return new SymeoDataProcessingJobApiProperties();
    }

    @Bean
    public SymeoHttpClient symeoHttpClient(final SymeoDataProcessingJobApiProperties symeoDataProcessingJobApiProperties,
                                           final HttpClient httpClient) {
        return new SymeoHttpClient(httpClient, symeoDataProcessingJobApiProperties);
    }

    @Bean
    public SymeoDataProcessingJobApiClientAdapter symeoDataProcessingJobApiAdapter(final SymeoHttpClient symeoHttpClient) {
        return new SymeoDataProcessingJobApiClientAdapter(symeoHttpClient);
    }
}
