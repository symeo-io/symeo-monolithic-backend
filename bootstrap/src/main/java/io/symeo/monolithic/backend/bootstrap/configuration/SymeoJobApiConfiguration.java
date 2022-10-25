package io.symeo.monolithic.backend.bootstrap.configuration;

import io.symeo.monolithic.backend.domain.bff.port.out.SymeoJobApiAdapter;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.SymeoHttpClient;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.SymeoJobApiProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.net.http.HttpClient;
import java.util.UUID;

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
        return new SymeoJobApiAdapter() {
            @Override
            public void startJobForOrganizationId(UUID organizationId) throws SymeoException {

            }

            @Override
            public void startJobForOrganizationIdAndTeamId(UUID organizationId, UUID teamId) throws SymeoException {

            }
        };
    }
}
