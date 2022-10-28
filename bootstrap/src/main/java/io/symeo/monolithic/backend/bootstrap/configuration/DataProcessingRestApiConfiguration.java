package io.symeo.monolithic.backend.bootstrap.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.symeo.monolithic.backend.domain.bff.port.in.OrganizationFacadeAdapter;
import io.symeo.monolithic.backend.github.webhook.api.adapter.GithubWebhookApiAdapter;
import io.symeo.monolithic.backend.github.webhook.api.adapter.properties.GithubWebhookProperties;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.SymeoDataProcessingJobApiProperties;
import io.symeo.monolithic.backend.job.domain.model.job.JobManager;
import io.symeo.monolithic.backend.job.domain.port.in.CommitTestingDataFacadeAdapter;
import io.symeo.monolithic.backend.job.domain.port.in.DataProcessingJobAdapter;
import io.symeo.monolithic.backend.job.domain.port.out.DataProcessingExpositionStorageAdapter;
import io.symeo.monolithic.backend.job.domain.port.out.DataProcessingJobStorage;
import io.symeo.monolithic.backend.job.domain.service.DataProcessingJobService;
import io.symeo.monolithic.backend.job.domain.service.VcsDataProcessingService;
import io.symeo.monolithic.backend.job.rest.api.adapter.DataProcessingRestApiAdapter;
import io.symeo.monolithic.backend.job.rest.api.adapter.TestingRestApiAdapter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@Profile("job-api")
public class DataProcessingRestApiConfiguration {


    @Bean
    public DataProcessingRestApiAdapter dataProcessingJobApi(final DataProcessingJobAdapter dataProcessingJobAdapter,
                                                             final SymeoDataProcessingJobApiProperties symeoDataProcessingJobApiProperties) {
        return new DataProcessingRestApiAdapter(dataProcessingJobAdapter, symeoDataProcessingJobApiProperties.getApiKey(),
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

    @Bean
    public DataProcessingJobAdapter dataProcessingJobAdapter(final DataProcessingExpositionStorageAdapter dataProcessingExpositionStorageAdapter,
                                                             final DataProcessingJobStorage dataProcessingJobStorage,
                                                             final VcsDataProcessingService vcsDataProcessingService,
                                                             final JobManager jobManager) {
        return new DataProcessingJobService(dataProcessingExpositionStorageAdapter, dataProcessingJobStorage,
                vcsDataProcessingService,
                jobManager);
    }
}
