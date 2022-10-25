package io.symeo.monolithic.backend.bootstrap.configuration;

import io.symeo.monolithic.backend.domain.bff.port.in.*;
import io.symeo.monolithic.backend.domain.bff.port.out.*;
import io.symeo.monolithic.backend.domain.bff.query.CurveQuery;
import io.symeo.monolithic.backend.domain.bff.query.HistogramQuery;
import io.symeo.monolithic.backend.domain.bff.service.insights.*;
import io.symeo.monolithic.backend.domain.bff.service.organization.*;
import io.symeo.monolithic.backend.domain.bff.service.vcs.PullRequestService;
import io.symeo.monolithic.backend.domain.bff.service.vcs.RepositoryService;
import io.symeo.monolithic.backend.domain.bff.storage.TeamStandardInMemoryStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfiguration {


    @Bean
    public RepositoryService repositoryService(final BffExpositionStorageAdapter expositionStorageAdapter) {
        return new RepositoryService(expositionStorageAdapter);
    }

    @Bean
    public HistogramQuery histogramQuery(final BffExpositionStorageAdapter expositionStorageAdapter,
                                         final TeamGoalFacadeAdapter teamGoalFacadeAdapter,
                                         final PullRequestHistogramService pullRequestHistogramService) {
        return new HistogramQuery(expositionStorageAdapter, teamGoalFacadeAdapter, pullRequestHistogramService);
    }

    @Bean
    public UserFacadeAdapter userFacadeAdapter(final UserStorageAdapter userStorageAdapter,
                                               final EmailDeliveryAdapter emailDeliveryAdapter) {
        return new UserService(userStorageAdapter, emailDeliveryAdapter);
    }

    @Bean
    public OrganizationFacadeAdapter organizationFacadeAdapter(final OrganizationStorageAdapter organizationStorageAdapter,
                                                               final SymeoJobApiAdapter symeoJobApiAdapter) {
        return new OrganizationService(organizationStorageAdapter, symeoJobApiAdapter);
    }

    @Bean
    public TeamFacadeAdapter teamFacadeAdapter(final TeamStorage teamStorage,
                                               final SymeoJobApiAdapter symeoJobApiAdapter) {
        return new TeamService(teamStorage, symeoJobApiAdapter);
    }

    @Bean
    public OnboardingFacadeAdapter onboardingFacadeAdapter(final OnboardingStorage onboardingStorage) {
        return new OnboardingService(onboardingStorage);
    }


    @Bean
    public CurveQuery curveQuery(final BffExpositionStorageAdapter expositionStorageAdapter,
                                 final TeamGoalFacadeAdapter teamGoalFacadeAdapter) {
        return new CurveQuery(expositionStorageAdapter, teamGoalFacadeAdapter);
    }

    @Bean
    public TeamStandardStorage teamStandardStorage() {
        return new TeamStandardInMemoryStorage();
    }

    @Bean
    public TeamGoalFacadeAdapter teamGoalFacadeAdapter(final TeamStandardStorage teamStandardStorage,
                                                       final TeamGoalStorage teamGoalStorage) {
        return new TeamGoalService(teamStandardStorage, teamGoalStorage);
    }

    @Bean
    public PullRequestHistogramService pullRequestHistogramService() {
        return new PullRequestHistogramService();
    }

    @Bean
    public PullRequestFacade pullRequestFacade(final BffExpositionStorageAdapter expositionStorageAdapter) {
        return new PullRequestService(expositionStorageAdapter);
    }

    @Bean
    public CycleTimeMetricsFacadeAdapter cycleTimeFacadeAdapter(final BffExpositionStorageAdapter expositionStorageAdapter,
                                                                final OrganizationSettingsFacade organizationSettingsFacade,
                                                                final CycleTimeService cycleTimeService) {
        return new CycleTimeMetricsMetricsService(expositionStorageAdapter, organizationSettingsFacade,
                cycleTimeService);
    }

    @Bean
    public OrganizationSettingsService organizationSettingsService(final BffExpositionStorageAdapter expositionStorageAdapter,
                                                                   final OrganizationStorageAdapter organizationStorageAdapter) {
        return new OrganizationSettingsService(expositionStorageAdapter, organizationStorageAdapter);
    }

    @Bean
    public CycleTimeService cycleTimeService() {
        return new CycleTimeService();
    }

    @Bean
    public DeploymentMetricsFacadeAdapter deploymentMetricsFacadeAdapter(final BffExpositionStorageAdapter expositionStorageAdapter,
                                                                         final OrganizationSettingsFacade organizationSettingsFacade,
                                                                         final DeploymentService deploymentService) {
        return new DeploymentMetricsService(expositionStorageAdapter, organizationSettingsFacade,
                deploymentService);
    }

    @Bean
    public DeploymentService deploymentService() {
        return new DeploymentService();
    }
}
