package fr.catlean.delivery.processor.bootstrap;

import fr.catlean.delivery.processor.bootstrap.configuration.CatleanDeliveryProcessorConfiguration;
import fr.catlean.delivery.processor.bootstrap.configuration.DomainConfiguration;
import fr.catlean.delivery.processor.bootstrap.configuration.GithubConfiguration;
import fr.catlean.delivery.processor.bootstrap.configuration.JsonLocalStorageConfiguration;
import fr.catlean.delivery.processor.domain.model.Repository;
import fr.catlean.delivery.processor.domain.service.DeliveryCommand;
import fr.catlean.delivery.processor.domain.service.DeliveryQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import java.util.List;

@SpringBootApplication
@EnableConfigurationProperties
@Import(value = {DomainConfiguration.class, CatleanDeliveryProcessorConfiguration.class, GithubConfiguration.class, JsonLocalStorageConfiguration.class})
@Slf4j
public class CatleanDeliveryProcessorApplication implements CommandLineRunner {

    private final DeliveryCommand deliveryCommand;
    private final DeliveryQuery deliveryQuery;

    public CatleanDeliveryProcessorApplication(DeliveryCommand deliveryCommand, DeliveryQuery deliveryQuery) {
        this.deliveryCommand = deliveryCommand;
        this.deliveryQuery = deliveryQuery;
    }

    public static void main(String[] args) {
        SpringApplication.run(CatleanDeliveryProcessorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        final String organisation = "armis-paris";
        deliveryCommand.collectRepositoriesForOrganisation(organisation);
        final List<Repository> repositories = deliveryQuery.readRepositoriesForOrganisation(organisation);
        LOGGER.info(repositories.toString());
        System.exit(0);
    }
}
