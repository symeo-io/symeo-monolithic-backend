package io.symeo.monolithic.backend.bootstrap.configuration;

import io.symeo.monolithic.backend.application.rest.api.adapter.api.*;
import io.symeo.monolithic.backend.application.rest.api.adapter.authentication.AuthenticationContextProvider;
import io.symeo.monolithic.backend.application.rest.api.adapter.authentication.AuthenticationService;
import io.symeo.monolithic.backend.application.rest.api.adapter.properties.RepositoryRetryProperties;
import io.symeo.monolithic.backend.application.rest.api.adapter.service.RepositoryRetryService;
import io.symeo.monolithic.backend.domain.bff.port.in.*;
import io.symeo.monolithic.backend.domain.bff.query.CurveQuery;
import io.symeo.monolithic.backend.domain.bff.query.HistogramQuery;
import io.symeo.monolithic.backend.domain.bff.service.organization.OrganizationSettingsService;
import io.symeo.monolithic.backend.domain.bff.service.vcs.RepositoryService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@Profile("front-api")
public class BffRestApiConfiguration {


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
                                                                 final UserFacadeAdapter userFacadeAdapter,
                                                                 final OrganizationSettingsService organizationSettingsService) {
        return new OrganizationRestApiAdapter(authenticationService, userFacadeAdapter, organizationSettingsService);
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
    public CycleTimeRestApiAdapter CycleTimeRestApiAdapter(final AuthenticationService authenticationService,
                                                           final CycleTimeMetricsFacadeAdapter cycleTimeMetricsFacadeAdapter,
                                                           final CycleTimeCurveFacadeAdapter cycleTimeCurveFacadeAdapter) {
        return new CycleTimeRestApiAdapter(authenticationService, cycleTimeMetricsFacadeAdapter, cycleTimeCurveFacadeAdapter);
    }

    @Bean
    public DeploymentRestApiAdapter DeploymentRestApiAdapter(final AuthenticationService authenticationService,
                                                             final DeploymentMetricsFacadeAdapter deploymentMetricsFacadeAdapter) {
        return new DeploymentRestApiAdapter(authenticationService, deploymentMetricsFacadeAdapter);
    }

    @Bean
    public TestingRestApiAdapter TestingRestApiAdapter(final AuthenticationService authenticationService,
                                                             final TestingMetricsFacadeAdapter testingMetricsFacadeAdapter) {
        return new TestingRestApiAdapter(authenticationService, testingMetricsFacadeAdapter);
    }
}
