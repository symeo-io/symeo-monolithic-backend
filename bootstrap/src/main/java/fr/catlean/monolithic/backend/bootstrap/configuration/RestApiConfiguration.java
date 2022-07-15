package fr.catlean.monolithic.backend.bootstrap.configuration;

import catlean.monolithic.backend.github.webhook.api.adapter.GithubWebhookApiAdapter;
import catlean.monolithic.backend.rest.api.adapter.DataProcessingRestApiAdapter;
import catlean.monolithic.backend.rest.api.adapter.PullRequestRestApiAdapter;
import catlean.monolithic.backend.rest.api.adapter.UserRestApiAdapter;
import catlean.monolithic.backend.rest.api.adapter.authentication.AuthenticationService;
import fr.catlean.monolithic.backend.domain.port.in.DataProcessingJobAdapter;
import fr.catlean.monolithic.backend.domain.port.in.OrganizationFacadeAdapter;
import fr.catlean.monolithic.backend.domain.port.in.UserFacadeAdapter;
import fr.catlean.monolithic.backend.domain.query.HistogramQuery;
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
    public AuthenticationService userAuthenticationService(final UserFacadeAdapter userFacadeAdapter) {
        return new AuthenticationService(userFacadeAdapter);
    }

    @Bean
    public GithubWebhookApiAdapter githubWebhookApiAdapter(final OrganizationFacadeAdapter organizationFacadeAdapter) {
        return new GithubWebhookApiAdapter(organizationFacadeAdapter);
    }
}
