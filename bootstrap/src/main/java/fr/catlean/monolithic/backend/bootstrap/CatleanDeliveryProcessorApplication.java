package fr.catlean.monolithic.backend.bootstrap;

import fr.catlean.monolithic.backend.bootstrap.configuration.*;
import fr.catlean.monolithic.backend.bootstrap.configuration.*;
import fr.catlean.monolithic.backend.domain.model.PullRequest;
import fr.catlean.monolithic.backend.domain.model.account.OrganizationAccount;
import fr.catlean.monolithic.backend.domain.port.out.OrganizationAccountAdapter;
import fr.catlean.monolithic.backend.domain.service.DeliveryProcessorService;
import fr.catlean.monolithic.backend.domain.service.PullRequestService;
import fr.catlean.monolithic.backend.infrastructure.postgres.configuration.PostgresConfiguration;
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
    private final OrganizationAccountAdapter organizationAccountAdapter;
    private final PullRequestService pullRequestService;

    public static void main(String[] args) {
        SpringApplication.run(CatleanDeliveryProcessorApplication.class, args);
    }

    @Override
    public void run(String... args) {
        final String organization = "dalma";
        final OrganizationAccount organizationAccount =
                organizationAccountAdapter.findOrganizationForName(organization);
        final List<PullRequest> pullRequestList =
                deliveryProcessorService.collectPullRequestsForOrganization(organizationAccount);
        LOGGER.info("{} PRs were collected", pullRequestList.size());
        pullRequestService.computeAndSavePullRequestSizeHistogram(pullRequestList, organizationAccount);
        pullRequestService.computeAndSavePullRequestTimeHistogram(pullRequestList, organizationAccount);
        System.exit(0);
    }
}
