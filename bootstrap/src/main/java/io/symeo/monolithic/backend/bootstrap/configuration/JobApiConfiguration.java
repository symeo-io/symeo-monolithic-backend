package io.symeo.monolithic.backend.bootstrap.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.symeo.monolithic.backend.domain.bff.port.in.OrganizationFacadeAdapter;
import io.symeo.monolithic.backend.github.webhook.api.adapter.GithubWebhookApiAdapter;
import io.symeo.monolithic.backend.github.webhook.api.adapter.properties.GithubWebhookProperties;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.SymeoJobApiProperties;
import io.symeo.monolithic.backend.job.domain.port.in.JobAdapter;
import io.symeo.monolithic.backend.job.rest.api.adapter.DataProcessingRestApiAdapter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@Profile("job-api")
public class JobApiConfiguration {


    @Bean
    public DataProcessingRestApiAdapter dataProcessingJobApi(final JobAdapter jobAdapter,
                                                             final SymeoJobApiProperties symeoJobApiProperties) {
        return new DataProcessingRestApiAdapter(jobAdapter, symeoJobApiProperties.getApiKey(),
                symeoJobApiProperties.getHeaderKey());
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
