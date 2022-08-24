package io.symeo.monolithic.backend.bootstrap.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.symeo.monolithic.backend.application.rest.api.adapter.api.DataProcessingRestApiAdapter;
import io.symeo.monolithic.backend.domain.port.in.DataProcessingJobAdapter;
import io.symeo.monolithic.backend.domain.port.in.OrganizationFacadeAdapter;
import io.symeo.monolithic.backend.github.webhook.api.adapter.GithubWebhookApiAdapter;
import io.symeo.monolithic.backend.github.webhook.api.adapter.properties.GithubWebhookProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@Profile("job")
public class JobApiConfiguration {


    @Bean
    public DataProcessingRestApiAdapter dataProcessingJobApi(final DataProcessingJobAdapter dataProcessingJobAdapter) {
        return new DataProcessingRestApiAdapter(dataProcessingJobAdapter);
    }

    @Bean
    @ConfigurationProperties("application.github.webhook")
    public GithubWebhookProperties githubWebhookProperties() {
        return new GithubWebhookProperties();
    }

    @Bean
    public GithubWebhookApiAdapter githubWebhookApiAdapter(final OrganizationFacadeAdapter organizationFacadeAdapter,
                                                           final ObjectMapper objectMapper,
                                                           final GithubWebhookProperties githubWebhookProperties) {
        return new GithubWebhookApiAdapter(organizationFacadeAdapter, githubWebhookProperties, objectMapper);
    }
}