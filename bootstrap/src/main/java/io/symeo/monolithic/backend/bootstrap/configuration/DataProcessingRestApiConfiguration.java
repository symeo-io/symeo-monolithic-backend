package io.symeo.monolithic.backend.bootstrap.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.symeo.monolithic.backend.domain.bff.port.in.OrganizationFacadeAdapter;
import io.symeo.monolithic.backend.github.webhook.api.adapter.GithubWebhookApiAdapter;
import io.symeo.monolithic.backend.github.webhook.api.adapter.properties.GithubWebhookProperties;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.SymeoDataProcessingJobApiProperties;
import io.symeo.monolithic.backend.job.domain.port.in.CommitTestingDataFacadeAdapter;
import io.symeo.monolithic.backend.job.domain.port.in.DataProcessingJobAdapter;
import io.symeo.monolithic.backend.job.domain.port.in.OrganizationJobFacade;
import io.symeo.monolithic.backend.job.rest.api.adapter.DataProcessingRestApiAdapter;
import io.symeo.monolithic.backend.job.rest.api.adapter.TestingRestApiAdapter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@Profile("job-api")
public class DataProcessingRestApiConfiguration {


    @Bean
    public DataProcessingRestApiAdapter dataProcessingJobApi(final DataProcessingJobAdapter dataProcessingJobAdapter,
                                                             final OrganizationJobFacade organizationJobFacade,
                                                             final SymeoDataProcessingJobApiProperties symeoDataProcessingJobApiProperties) {
        return new DataProcessingRestApiAdapter(dataProcessingJobAdapter, organizationJobFacade,
                symeoDataProcessingJobApiProperties.getApiKey(),
                symeoDataProcessingJobApiProperties.getHeaderKey());
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

    @Bean
    public TestingRestApiAdapter testingRestApiAdapter(final CommitTestingDataFacadeAdapter commitTestingDataFacadeAdapter,
                                                       final OrganizationFacadeAdapter organizationFacadeAdapter) {
        return new TestingRestApiAdapter(commitTestingDataFacadeAdapter, organizationFacadeAdapter);
    }
}
