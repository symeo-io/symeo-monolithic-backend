package fr.catlean.delivery.processor.bootstrap;

import fr.catlean.delivery.processor.bootstrap.configuration.CatleanDeliveryProcessorConfiguration;
import fr.catlean.delivery.processor.bootstrap.configuration.DomainConfiguration;
import fr.catlean.delivery.processor.bootstrap.configuration.GithubConfiguration;
import fr.catlean.delivery.processor.bootstrap.configuration.JsonLocalStorageConfiguration;
import fr.catlean.delivery.processor.domain.model.PullRequest;
import fr.catlean.delivery.processor.domain.service.DeliveryProcessorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import java.util.List;

@SpringBootApplication
@EnableConfigurationProperties
@Import(value = {DomainConfiguration.class, CatleanDeliveryProcessorConfiguration.class, GithubConfiguration.class,
        JsonLocalStorageConfiguration.class})
@Slf4j
public class CatleanDeliveryProcessorApplication implements CommandLineRunner {

    private final DeliveryProcessorService deliveryProcessorService;

    public CatleanDeliveryProcessorApplication(DeliveryProcessorService deliveryProcessorService) {
        this.deliveryProcessorService = deliveryProcessorService;
    }

    public static void main(String[] args) {
        SpringApplication.run(CatleanDeliveryProcessorApplication.class, args);
    }

    @Override
    public void run(String... args) {
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "4");
        final String organisation = "armis-paris";
        final List<PullRequest> pullRequestList =
                deliveryProcessorService.collectPullRequestsForOrganisation(organisation);
        LOGGER.info("{} PRs were collected", pullRequestList.size());
        System.exit(0);
    }
}
