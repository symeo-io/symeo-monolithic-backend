package fr.catlean.monolithic.backend.bootstrap.configuration;

import catlean.monolithic.backend.github.webhook.api.adapter.GithubWebhookApiAdapter;
import catlean.monolithic.backend.github.webhook.api.adapter.properties.GithubWebhookProperties;
import catlean.monolithic.backend.rest.api.adapter.DataProcessingRestApiAdapter;
import catlean.monolithic.backend.rest.api.adapter.PullRequestRestApiAdapter;
import catlean.monolithic.backend.rest.api.adapter.RepositoryRestApiAdapter;
import catlean.monolithic.backend.rest.api.adapter.UserRestApiAdapter;
import catlean.monolithic.backend.rest.api.adapter.authentication.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.catlean.monolithic.backend.domain.port.in.DataProcessingJobAdapter;
import fr.catlean.monolithic.backend.domain.port.in.OrganizationFacadeAdapter;
import fr.catlean.monolithic.backend.domain.port.in.UserFacadeAdapter;
import fr.catlean.monolithic.backend.domain.query.HistogramQuery;
import fr.catlean.monolithic.backend.domain.service.RepositoryService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

public class RestApiConfiguration {

    @Bean
    public DataProcessingRestApiAdapter dataProcessingJobApi(final DataProcessingJobAdapter dataProcessingJobAdapter) {
        return new DataProcessingRestApiAdapter(dataProcessingJobAdapter);
    }

    @Bean
    public PullRequestRestApiAdapter pullRequestRestApiAdapter(final HistogramQuery histogramQuery) {
        return new PullRequestRestApiAdapter(histogramQuery);
    }

    @Bean
    public UserRestApiAdapter userRestApiAdapter(final AuthenticationService authenticationService,
                                                 final UserFacadeAdapter userFacadeAdapter) {
        return new UserRestApiAdapter(authenticationService, userFacadeAdapter);
    }

    @Bean
    public RepositoryRestApiAdapter repositoryRestApiAdapter(final AuthenticationService authenticationService,
                                                             final RepositoryService repositoryService) {
        return new RepositoryRestApiAdapter(authenticationService, repositoryService);
    }

    @Bean
    public AuthenticationService userAuthenticationService(final UserFacadeAdapter userFacadeAdapter) {
        return new AuthenticationService(userFacadeAdapter);
    }

    @Bean
    @ConfigurationProperties("github.webhook")
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
