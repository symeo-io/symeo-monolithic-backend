package io.symeo.monolithic.backend.bootstrap.configuration;

import io.symeo.monolithic.backend.domain.bff.model.metric.AverageCycleTimeFactory;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTimeFactory;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTimePieceFactory;
import io.symeo.monolithic.backend.domain.bff.port.in.*;
import io.symeo.monolithic.backend.domain.bff.port.out.*;
import io.symeo.monolithic.backend.domain.bff.query.CurveQuery;
import io.symeo.monolithic.backend.domain.bff.query.HistogramQuery;
import io.symeo.monolithic.backend.domain.bff.service.insights.*;
import io.symeo.monolithic.backend.domain.bff.service.organization.*;
import io.symeo.monolithic.backend.domain.bff.service.service.JobService;
import io.symeo.monolithic.backend.domain.bff.service.vcs.PullRequestService;
import io.symeo.monolithic.backend.domain.bff.service.vcs.RepositoryService;
import io.symeo.monolithic.backend.domain.bff.storage.TeamStandardInMemoryStorage;
import io.symeo.monolithic.backend.job.domain.port.in.CommitTestingDataFacadeAdapter;
import io.symeo.monolithic.backend.job.domain.port.out.CommitTestingDataStorage;
import io.symeo.monolithic.backend.job.domain.service.CommitTestingDataService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BffDomainConfiguration {


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
                                                               final OrganizationApiKeyStorageAdapter organizationApiKeyStorageAdapter,
                                                               final BffSymeoDataProcessingJobApiAdapter bffSymeoDataProcessingJobApiAdapter) {
        return new OrganizationService(organizationStorageAdapter, organizationApiKeyStorageAdapter,
                bffSymeoDataProcessingJobApiAdapter);
    }

    @Bean
    public TeamFacadeAdapter teamFacadeAdapter(final TeamStorage teamStorage,
                                               final BffSymeoDataProcessingJobApiAdapter bffSymeoDataProcessingJobApiAdapter) {
        return new TeamService(teamStorage, bffSymeoDataProcessingJobApiAdapter);
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
        return new CycleTimeMetricsService(expositionStorageAdapter, organizationSettingsFacade,
                cycleTimeService);
    }

    @Bean
    CycleTimeCurveFacadeAdapter cycleTimeCurveFacadeAdapter(final OrganizationSettingsFacade organizationSettingsFacade,
                                                            final BffExpositionStorageAdapter bffExpositionStorageAdapter,
                                                            final CycleTimeFactory cycleTimeFactory) {
        return new CycleTimeCurveService(organizationSettingsFacade, bffExpositionStorageAdapter, cycleTimeFactory);
    }

    @Bean
    public OrganizationSettingsService organizationSettingsService(final BffExpositionStorageAdapter expositionStorageAdapter,
                                                                   final OrganizationStorageAdapter organizationStorageAdapter) {
        return new OrganizationSettingsService(expositionStorageAdapter, organizationStorageAdapter);
    }

    @Bean
    public CycleTimeService cycleTimeService(final AverageCycleTimeFactory averageCycleTimeFactory,
                                             final CycleTimePieceFactory cycleTimePieceFactory) {
        return new CycleTimeService(averageCycleTimeFactory, cycleTimePieceFactory);
    }

    @Bean
    public AverageCycleTimeFactory averageCycleTimeFactory(final CycleTimeFactory cycleTimeFactory) {
        return new AverageCycleTimeFactory(cycleTimeFactory);
    }

    @Bean
    public CycleTimePieceFactory cycleTimePieceFactory(final CycleTimeFactory cycleTimeFactory) {
        return new CycleTimePieceFactory(cycleTimeFactory);
    }

    @Bean
    public CycleTimeFactory cycleTimeFactory() {
        return new CycleTimeFactory();
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

    @Bean
    public CommitTestingDataFacadeAdapter commitTestingDataFacadeAdapter(final CommitTestingDataStorage commitTestingDataStorage) {
        return new CommitTestingDataService(commitTestingDataStorage);
    }

    @Bean
    public JobFacadeAdapter jobFacadeAdapter(final BffJobStorage jobStorage) {
        return new JobService(jobStorage);
    }


}