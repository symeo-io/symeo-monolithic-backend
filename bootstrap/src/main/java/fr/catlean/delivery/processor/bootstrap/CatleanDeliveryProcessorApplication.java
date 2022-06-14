package fr.catlean.delivery.processor.bootstrap;

import fr.catlean.delivery.processor.bootstrap.configuration.*;
import fr.catlean.delivery.processor.domain.model.PullRequest;
import fr.catlean.delivery.processor.domain.model.account.OrganisationAccount;
import fr.catlean.delivery.processor.domain.port.out.OrganisationAccountAdapter;
import fr.catlean.delivery.processor.domain.service.DeliveryProcessorService;
import fr.catlean.delivery.processor.domain.service.PullRequestService;
import fr.catlean.delivery.processor.infrastructure.postgres.configuration.PostgresConfiguration;
import lombok.AllArgsConstructor;
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
        JsonLocalStorageConfiguration.class, PostgresConfiguration.class, AccountConfiguration.class})
@Slf4j
@AllArgsConstructor
public class CatleanDeliveryProcessorApplication implements CommandLineRunner {

    private final DeliveryProcessorService deliveryProcessorService;
    private final OrganisationAccountAdapter organisationAccountAdapter;
    private final PullRequestService pullRequestService;

    public static void main(String[] args) {
        SpringApplication.run(CatleanDeliveryProcessorApplication.class, args);
    }

    @Override
    public void run(String... args) {
        final String organisation = "armis";
        final OrganisationAccount organisationAccount =
                organisationAccountAdapter.findOrganisationForName(organisation);
        final List<PullRequest> pullRequestList =
                deliveryProcessorService.collectPullRequestsForOrganisation(organisationAccount);
        LOGGER.info("{} PRs were collected", pullRequestList.size());
        pullRequestService.computeAndSavePullRequestSizeHistogram(pullRequestList, organisationAccount);
        pullRequestService.computeAndSavePullRequestTimeHistogram(pullRequestList, organisationAccount);
        System.exit(0);
    }
}
