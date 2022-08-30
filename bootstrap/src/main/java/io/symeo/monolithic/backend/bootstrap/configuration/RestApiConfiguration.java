package io.symeo.monolithic.backend.bootstrap.configuration;

import io.symeo.monolithic.backend.application.rest.api.adapter.api.*;
import io.symeo.monolithic.backend.application.rest.api.adapter.authentication.AuthenticationContextProvider;
import io.symeo.monolithic.backend.application.rest.api.adapter.authentication.AuthenticationService;
import io.symeo.monolithic.backend.application.rest.api.adapter.properties.RepositoryRetryProperties;
import io.symeo.monolithic.backend.application.rest.api.adapter.service.RepositoryRetryService;
import io.symeo.monolithic.backend.domain.port.in.*;
import io.symeo.monolithic.backend.domain.query.CurveQuery;
import io.symeo.monolithic.backend.domain.query.HistogramQuery;
import io.symeo.monolithic.backend.domain.service.insights.LeadTimeService;
import io.symeo.monolithic.backend.domain.service.platform.vcs.RepositoryService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@Profile("front-api")
public class RestApiConfiguration {


    @Bean
    public TeamGoalRestApiAdapter teamGoalRestApiAdapter(final AuthenticationService authenticationService,
                                                         final HistogramQuery histogramQuery,
                                                         final CurveQuery curveQuery,
                                                         final TeamGoalFacadeAdapter teamGoalFacadeAdapter) {
        return new TeamGoalRestApiAdapter(histogramQuery, authenticationService, curveQuery, teamGoalFacadeAdapter);
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
    public OrganizationRestApiAdapter organizationRestApiAdapter(final AuthenticationService authenticationService,
                                                                 final UserFacadeAdapter userFacadeAdapter) {
        return new OrganizationRestApiAdapter(authenticationService, userFacadeAdapter);
    }

    @Bean
    public JobRestApiAdapter jobRestApiAdapter(final AuthenticationService authenticationService,
                                               final JobFacadeAdapter jobFacadeAdapter) {
        return new JobRestApiAdapter(authenticationService, jobFacadeAdapter);
    }

    @Bean
    public PullRequestsRestApiAdapter pullRequestsRestApiAdapter(final AuthenticationService authenticationService,
                                                                 final PullRequestFacade pullRequestFacade) {
        return new PullRequestsRestApiAdapter(authenticationService, pullRequestFacade);
    }

    @Bean
    public LeadTimeRestApiAdapter leadTimeRestApiAdapter(final AuthenticationService authenticationService,
                                                         final LeadTimeFacadeAdapter leadTimeFacadeAdapter) {
        return new LeadTimeRestApiAdapter(authenticationService, leadTimeFacadeAdapter);
    }
}
