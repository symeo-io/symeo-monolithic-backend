package fr.catlean.monolithic.backend.bootstrap.configuration;

import fr.catlean.monolithic.backend.domain.command.DeliveryCommand;
import fr.catlean.monolithic.backend.domain.job.JobManager;
import fr.catlean.monolithic.backend.domain.port.in.*;
import fr.catlean.monolithic.backend.domain.port.out.*;
import fr.catlean.monolithic.backend.domain.query.CurveQuery;
import fr.catlean.monolithic.backend.domain.query.DeliveryQuery;
import fr.catlean.monolithic.backend.domain.query.HistogramQuery;
import fr.catlean.monolithic.backend.domain.service.DataProcessingJobService;
import fr.catlean.monolithic.backend.domain.service.account.*;
import fr.catlean.monolithic.backend.domain.service.insights.PullRequestHistogramService;
import fr.catlean.monolithic.backend.domain.service.platform.vcs.RepositoryService;
import fr.catlean.monolithic.backend.domain.service.platform.vcs.VcsService;
import fr.catlean.monolithic.backend.domain.storage.TeamStandardInMemoryStorage;
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
    public DeliveryQuery deliveryQuery(final RawStorageAdapter rawStorageAdapter,
                                       final VersionControlSystemAdapter versionControlSystemAdapter) {
        return new DeliveryQuery(rawStorageAdapter, versionControlSystemAdapter);
    }

    @Bean
    public VcsService deliveryProcessorService(final DeliveryCommand deliveryCommand,
                                               final DeliveryQuery deliveryQuery,
                                               final ExpositionStorageAdapter expositionStorageAdapter) {
        return new VcsService(deliveryCommand, deliveryQuery, expositionStorageAdapter);
    }


    @Bean
    public DataProcessingJobService dataProcessingJobService(final VcsService vcsService,
                                                             final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter,
                                                             final RepositoryService repositoryService,
                                                             final JobManager jobManager) {
        return new DataProcessingJobService(vcsService, accountOrganizationStorageAdapter,
                repositoryService, jobManager);
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
    public OrganizationFacadeAdapter organizationFacadeAdapter(final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter,
                                                               final DataProcessingJobAdapter dataProcessingJobAdapter) {
        return new OrganizationService(accountOrganizationStorageAdapter, dataProcessingJobAdapter);
    }

    @Bean
    public TeamFacadeAdapter teamFacadeAdapter(final AccountTeamStorage accountTeamStorage) {
        return new TeamService(accountTeamStorage);
    }

    @Bean
    public OnboardingFacadeAdapter onboardingFacadeAdapter(final AccountOnboardingStorage accountOnboardingStorage) {
        return new OnboardingService(accountOnboardingStorage);
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
}
