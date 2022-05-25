package fr.catlean.delivery.processor.bootstrap.configuration;

import fr.catlean.delivery.processor.domain.command.DeliveryCommand;
import fr.catlean.delivery.processor.domain.port.out.RawStorageAdapter;
import fr.catlean.delivery.processor.domain.port.out.VersionControlSystemAdapter;
import fr.catlean.delivery.processor.domain.query.DeliveryQuery;
import fr.catlean.delivery.processor.domain.service.DeliveryProcessorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfiguration {

    @Bean
    public DeliveryCommand deliveryCommand(
            RawStorageAdapter rawStorageAdapter,
            VersionControlSystemAdapter versionControlSystemAdapter) {
        return new DeliveryCommand(rawStorageAdapter, versionControlSystemAdapter);
    }

    @Bean
    public DeliveryQuery deliveryQuery(RawStorageAdapter rawStorageAdapter,
                                       VersionControlSystemAdapter versionControlSystemAdapter) {
        return new DeliveryQuery(rawStorageAdapter, versionControlSystemAdapter);
    }

    @Bean
    public DeliveryProcessorService deliveryProcessorService(DeliveryCommand deliveryCommand,
                                                             DeliveryQuery deliveryQuery) {
        return new DeliveryProcessorService(deliveryCommand, deliveryQuery);
    }
}
