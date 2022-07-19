package fr.catlean.monolithic.backend.bootstrap.configuration;

import fr.catlean.monolithic.backend.domain.command.DeliveryCommand;
import fr.catlean.monolithic.backend.domain.port.in.OrganizationFacadeAdapter;
import fr.catlean.monolithic.backend.domain.port.in.UserFacadeAdapter;
import fr.catlean.monolithic.backend.domain.port.out.*;
import fr.catlean.monolithic.backend.domain.query.DeliveryQuery;
import fr.catlean.monolithic.backend.domain.query.HistogramQuery;
import fr.catlean.monolithic.backend.domain.service.*;
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
    public DeliveryProcessorService deliveryProcessorService(final DeliveryCommand deliveryCommand,
                                                             final DeliveryQuery deliveryQuery) {
        return new DeliveryProcessorService(deliveryCommand, deliveryQuery);
    }

    @Bean
    public PullRequestService pullRequestSizeService(final ExpositionStorageAdapter expositionStorageAdapter) {
        return new PullRequestService(expositionStorageAdapter);
    }


    @Bean
    public DataProcessingJobService dataProcessingJobService(final DeliveryProcessorService deliveryProcessorService,
                                                             final PullRequestService pullRequestService,
                                                             final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter,
                                                             final RepositoryService repositoryService) {
        return new DataProcessingJobService(deliveryProcessorService, accountOrganizationStorageAdapter, pullRequestService,
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
}
