package io.symeo.monolithic.backend.bootstrap.configuration;

import io.symeo.monolithic.backend.domain.port.out.SymeoJobApiAdapter;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.SymeoHttpClient;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.SymeoJobApiClientAdapter;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.SymeoJobApiProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.net.http.HttpClient;

public class SymeoJobApiConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "application.job-api")
    public SymeoJobApiProperties symeoJobApiProperties() {
        return new SymeoJobApiProperties();
    }

    @Bean
    public SymeoHttpClient symeoHttpClient(final SymeoJobApiProperties symeoJobApiProperties,
                                           final HttpClient httpClient) {
        return new SymeoHttpClient(httpClient, symeoJobApiProperties);
    }

    @Bean
    public SymeoJobApiAdapter symeoJobApiAdapter(final SymeoHttpClient symeoHttpClient) {
        return new SymeoJobApiClientAdapter(symeoHttpClient);
    }
}
