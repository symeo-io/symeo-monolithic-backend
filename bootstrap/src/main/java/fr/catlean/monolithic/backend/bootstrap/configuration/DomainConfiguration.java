package fr.catlean.monolithic.backend.bootstrap.configuration;

import fr.catlean.monolithic.backend.domain.command.DeliveryCommand;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorage;
import fr.catlean.monolithic.backend.domain.port.out.RawStorageAdapter;
import fr.catlean.monolithic.backend.domain.port.out.VersionControlSystemAdapter;
import fr.catlean.monolithic.backend.domain.query.DeliveryQuery;
import fr.catlean.monolithic.backend.domain.service.DeliveryProcessorService;
import fr.catlean.monolithic.backend.domain.service.PullRequestService;
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
                                                             final ExpositionStorage expositionStorage) {
        return new DeliveryProcessorService(deliveryCommand, deliveryQuery, expositionStorage);
    }

    @Bean
    public PullRequestService pullRequestSizeService(final ExpositionStorage expositionStorage) {
        return new PullRequestService(expositionStorage);
    }
}
