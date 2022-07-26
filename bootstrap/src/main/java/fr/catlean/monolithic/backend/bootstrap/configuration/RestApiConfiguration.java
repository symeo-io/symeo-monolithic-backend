package fr.catlean.monolithic.backend.bootstrap.configuration;

import catlean.monolithic.backend.github.webhook.api.adapter.GithubWebhookApiAdapter;
import catlean.monolithic.backend.github.webhook.api.adapter.properties.GithubWebhookProperties;
import catlean.monolithic.backend.rest.api.adapter.*;
import catlean.monolithic.backend.rest.api.adapter.authentication.AuthenticationContextProvider;
import catlean.monolithic.backend.rest.api.adapter.authentication.AuthenticationService;
import catlean.monolithic.backend.rest.api.adapter.properties.RepositoryRetryProperties;
import catlean.monolithic.backend.rest.api.adapter.service.RepositoryRetryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.catlean.monolithic.backend.domain.port.in.*;
import fr.catlean.monolithic.backend.domain.query.HistogramQuery;
import fr.catlean.monolithic.backend.domain.service.platform.vcs.RepositoryService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

public class RestApiConfiguration {

    @Bean
    public DataProcessingRestApiAdapter dataProcessingJobApi(final DataProcessingJobAdapter dataProcessingJobAdapter) {
        return new DataProcessingRestApiAdapter(dataProcessingJobAdapter);
    }

    @Bean
    public TimeToMergeRestApiAdapter timeToMergeRestApiAdapter(final AuthenticationService authenticationService,
                                                               final HistogramQuery histogramQuery) {
        return new TimeToMergeRestApiAdapter(histogramQuery, authenticationService);
    }

    @Bean
    public UserRestApiAdapter userRestApiAdapter(final AuthenticationService authenticationService,
                                                 final UserFacadeAdapter userFacadeAdapter,
                                                 final OnboardingFacadeAdapter onboardingFacadeAdapter) {
        return new UserRestApiAdapter(authenticationService, userFacadeAdapter, onboardingFacadeAdapter);
    }

    @Bean
    @ConfigurationProperties(prefix = "application.frontend.api.repositories")
    public RepositoryRetryProperties repositoryRetryProperties() {
        return new RepositoryRetryProperties();
    }

    @Bean
    public RepositoryRetryService repositoryRetryService(final RepositoryRetryProperties repositoryRetryProperties,
                                                         final JobFacadeAdapter jobFacadeAdapter) {
        return new RepositoryRetryService(jobFacadeAdapter, repositoryRetryProperties);
    }

    @Bean
    public RepositoryRestApiAdapter repositoryRestApiAdapter(final AuthenticationService authenticationService,
                                                             final RepositoryService repositoryService,
                                                             final RepositoryRetryService repositoryRetryService) {
        return new RepositoryRestApiAdapter(authenticationService, repositoryService, repositoryRetryService);
    }

    @Bean
    public TeamRestApiAdapter teamRestApiAdapter(final AuthenticationService authenticationService,
                                                 final TeamFacadeAdapter teamFacadeAdapter) {
        return new TeamRestApiAdapter(authenticationService, teamFacadeAdapter);
    }

    @Bean
    public AuthenticationService userAuthenticationService(final UserFacadeAdapter userFacadeAdapter,
                                                           final AuthenticationContextProvider authenticationContextProvider) {
        return new AuthenticationService(userFacadeAdapter, authenticationContextProvider);
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
    public OrganizationRestApiAdapter organizationRestApiAdapter(final AuthenticationService authenticationService,
                                                                 final UserFacadeAdapter userFacadeAdapter){
        return new OrganizationRestApiAdapter(authenticationService, userFacadeAdapter);
    }
}
