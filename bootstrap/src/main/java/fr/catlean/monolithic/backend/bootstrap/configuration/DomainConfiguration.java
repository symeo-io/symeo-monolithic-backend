package fr.catlean.monolithic.backend.bootstrap.configuration;

import fr.catlean.monolithic.backend.domain.command.DeliveryCommand;
import fr.catlean.monolithic.backend.domain.job.DataProcessingJobService;
import fr.catlean.monolithic.backend.domain.port.in.OnboardingFacadeAdapter;
import fr.catlean.monolithic.backend.domain.port.in.OrganizationFacadeAdapter;
import fr.catlean.monolithic.backend.domain.port.in.TeamFacadeAdapter;
import fr.catlean.monolithic.backend.domain.port.in.UserFacadeAdapter;
import fr.catlean.monolithic.backend.domain.port.out.*;
import fr.catlean.monolithic.backend.domain.query.DeliveryQuery;
import fr.catlean.monolithic.backend.domain.query.HistogramQuery;
import fr.catlean.monolithic.backend.domain.service.account.OnboardingService;
import fr.catlean.monolithic.backend.domain.service.account.OrganizationService;
import fr.catlean.monolithic.backend.domain.service.account.TeamService;
import fr.catlean.monolithic.backend.domain.service.account.UserService;
import fr.catlean.monolithic.backend.domain.service.insights.PullRequesHistogramService;
import fr.catlean.monolithic.backend.domain.service.platform.vcs.RepositoryService;
import fr.catlean.monolithic.backend.domain.service.platform.vcs.VcsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                                               final DeliveryQuery deliveryQuery) {
        return new VcsService(deliveryCommand, deliveryQuery);
    }

    @Bean
    public PullRequesHistogramService pullRequestSizeService(final ExpositionStorageAdapter expositionStorageAdapter) {
        return new PullRequesHistogramService(expositionStorageAdapter);
    }


    @Bean
    public DataProcessingJobService dataProcessingJobService(final VcsService vcsService,
                                                             final PullRequesHistogramService pullRequesHistogramService,
                                                             final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter,
                                                             final RepositoryService repositoryService) {
        return new DataProcessingJobService(vcsService, accountOrganizationStorageAdapter, pullRequesHistogramService,
                repositoryService);
    }

    @Bean
    public RepositoryService repositoryService(final ExpositionStorageAdapter expositionStorageAdapter) {
        return new RepositoryService(expositionStorageAdapter);
    }

    @Bean
    public HistogramQuery histogramQuery(final ExpositionStorageAdapter expositionStorageAdapter,
                                         final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter) {
        return new HistogramQuery(expositionStorageAdapter, accountOrganizationStorageAdapter);
    }

    @Bean
    public UserFacadeAdapter userFacadeAdapter(final UserStorageAdapter userStorageAdapter) {
        return new UserService(userStorageAdapter);
    }

    @Bean
    public OrganizationFacadeAdapter organizationFacadeAdapter(final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter) {
        return new OrganizationService(accountOrganizationStorageAdapter);
    }

    @Bean
    public TeamFacadeAdapter teamFacadeAdapter(final AccountTeamStorage accountTeamStorage){
        return new TeamService(accountTeamStorage);
    }

    @Bean
    public OnboardingFacadeAdapter onboardingFacadeAdapter(final AccountOnboardingStorage accountOnboardingStorage) {
        return new OnboardingService(accountOnboardingStorage);
    }
}
