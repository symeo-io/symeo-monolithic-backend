package io.symeo.monolithic.backend.bootstrap.configuration;

import io.symeo.monolithic.backend.domain.command.DeliveryCommand;
import io.symeo.monolithic.backend.domain.job.JobManager;
import io.symeo.monolithic.backend.domain.port.in.*;
import io.symeo.monolithic.backend.domain.port.out.*;
import io.symeo.monolithic.backend.domain.query.CurveQuery;
import io.symeo.monolithic.backend.domain.query.HistogramQuery;
import io.symeo.monolithic.backend.domain.service.DataProcessingJobService;
import io.symeo.monolithic.backend.domain.service.OrganizationSettingsService;
import io.symeo.monolithic.backend.domain.service.account.*;
import io.symeo.monolithic.backend.domain.service.insights.CycleTimeMetricsMetricsService;
import io.symeo.monolithic.backend.domain.service.insights.CycleTimeService;
import io.symeo.monolithic.backend.domain.service.insights.PullRequestHistogramService;
import io.symeo.monolithic.backend.domain.service.job.JobService;
import io.symeo.monolithic.backend.domain.service.platform.vcs.PullRequestService;
import io.symeo.monolithic.backend.domain.service.platform.vcs.RepositoryService;
import io.symeo.monolithic.backend.domain.service.platform.vcs.VcsService;
import io.symeo.monolithic.backend.domain.storage.TeamStandardInMemoryStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;

@Configuration
public class DomainConfiguration {

    @Bean
    public DeliveryCommand deliveryCommand(
            final RawStorageAdapter rawStorageAdapter,
            final VersionControlSystemAdapter versionControlSystemAdapter) {
        return new DeliveryCommand(rawStorageAdapter, versionControlSystemAdapter);
    }

    @Bean
    public VcsService deliveryProcessorService(final DeliveryCommand deliveryCommand,
                                               final ExpositionStorageAdapter expositionStorageAdapter) {
        return new VcsService(deliveryCommand, expositionStorageAdapter);
    }


    @Bean
    public DataProcessingJobService dataProcessingJobService(final VcsService vcsService,
                                                             final OrganizationStorageAdapter organizationStorageAdapter,
                                                             final RepositoryService repositoryService,
                                                             final JobManager jobManager,
                                                             final SymeoJobApiAdapter symeoJobApiAdapter,
                                                             final OrganizationSettingsService organizationSettingsService,
                                                             final ExpositionStorageAdapter expositionStorageAdapter,
                                                             final JobStorage jobStorage) {
        return new DataProcessingJobService(vcsService, organizationStorageAdapter,
                repositoryService, jobManager, symeoJobApiAdapter, organizationSettingsService,
                expositionStorageAdapter, jobStorage);
    }

    @Bean
    public RepositoryService repositoryService(final ExpositionStorageAdapter expositionStorageAdapter) {
        return new RepositoryService(expositionStorageAdapter);
    }

    @Bean
    public HistogramQuery histogramQuery(final ExpositionStorageAdapter expositionStorageAdapter,
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
                                                               final DataProcessingJobAdapter dataProcessingJobAdapter) {
        return new OrganizationService(organizationStorageAdapter, dataProcessingJobAdapter);
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
    public JobManager jobManager(final Executor executor, final JobStorage jobStorage) {
        return new JobManager(executor, jobStorage);
    }

    @Bean
    public CurveQuery curveQuery(final ExpositionStorageAdapter expositionStorageAdapter,
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
    public PullRequestFacade pullRequestFacade(final ExpositionStorageAdapter expositionStorageAdapter) {
        return new PullRequestService(expositionStorageAdapter);
    }

    @Bean
    public CycleTimeMetricsFacadeAdapter cycleTimeFacadeAdapter(final ExpositionStorageAdapter expositionStorageAdapter,
                                                                final OrganizationSettingsFacade organizationSettingsFacade,
                                                                final CycleTimeService cycleTimeService) {
        return new CycleTimeMetricsMetricsService(expositionStorageAdapter, organizationSettingsFacade,
                cycleTimeService);
    }

    @Bean
    public OrganizationSettingsService organizationSettingsService(final ExpositionStorageAdapter expositionStorageAdapter,
                                                                   final OrganizationStorageAdapter organizationStorageAdapter) {
        return new OrganizationSettingsService(expositionStorageAdapter, organizationStorageAdapter);
    }

    @Bean
    public JobFacadeAdapter jobFacadeAdapter(final JobStorage jobStorage) {
        return new JobService(jobStorage);
    }

    @Bean
    public CycleTimeService cycleTimeService() {
        return new CycleTimeService();
    }
}
