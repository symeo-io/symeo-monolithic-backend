package fr.catlean.monolithic.backend.bootstrap.configuration;

import fr.catlean.monolithic.backend.domain.command.DeliveryCommand;
import fr.catlean.monolithic.backend.domain.port.in.UserFacadeAdapter;
import fr.catlean.monolithic.backend.domain.port.out.*;
import fr.catlean.monolithic.backend.domain.query.DeliveryQuery;
import fr.catlean.monolithic.backend.domain.query.HistogramQuery;
import fr.catlean.monolithic.backend.domain.service.DataProcessingJobService;
import fr.catlean.monolithic.backend.domain.service.DeliveryProcessorService;
import fr.catlean.monolithic.backend.domain.service.PullRequestService;
import fr.catlean.monolithic.backend.domain.service.UserService;
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
                                                             final DeliveryQuery deliveryQuery,
                                                             final ExpositionStorageAdapter expositionStorageAdapter) {
        return new DeliveryProcessorService(deliveryCommand, deliveryQuery, expositionStorageAdapter);
    }

    @Bean
    public PullRequestService pullRequestSizeService(final ExpositionStorageAdapter expositionStorageAdapter) {
        return new PullRequestService(expositionStorageAdapter);
    }


    @Bean
    public DataProcessingJobService dataProcessingJobService(final DeliveryProcessorService deliveryProcessorService,
                                                             final PullRequestService pullRequestService,
                                                             final OrganizationAccountAdapter organizationAccountAdapter) {
        return new DataProcessingJobService(deliveryProcessorService, organizationAccountAdapter, pullRequestService);
    }

    @Bean
    public HistogramQuery histogramQuery(final ExpositionStorageAdapter expositionStorageAdapter,
                                         final OrganizationAccountAdapter organizationAccountAdapter) {
        return new HistogramQuery(expositionStorageAdapter, organizationAccountAdapter);
    }

    @Bean
    public UserFacadeAdapter userFacadeAdapter(final AccountStorageAdapter accountStorageAdapter) {
        return new UserService(accountStorageAdapter);
    }
}
