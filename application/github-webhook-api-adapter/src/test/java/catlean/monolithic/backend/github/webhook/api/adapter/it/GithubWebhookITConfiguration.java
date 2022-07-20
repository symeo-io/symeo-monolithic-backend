package catlean.monolithic.backend.github.webhook.api.adapter.it;

import catlean.monolithic.backend.github.webhook.api.adapter.GithubWebhookApiAdapter;
import catlean.monolithic.backend.github.webhook.api.adapter.properties.GithubWebhookProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.port.in.OrganizationFacadeAdapter;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class GithubWebhookITConfiguration {

    @Bean
    @ConfigurationProperties("github.webhook")
    public GithubWebhookProperties githubWebhookProperties() {
        return new GithubWebhookProperties();
    }

    @Bean
    public OrganizationAdapterMock organizationFacadeAdapter() {
        return new OrganizationAdapterMock();
    }

    @Bean
    public GithubWebhookApiAdapter githubWebhookApiAdapter(final ObjectMapper objectMapper,
                                                           final OrganizationFacadeAdapter organizationFacadeAdapter,
                                                           final GithubWebhookProperties githubWebhookProperties) {
        return new GithubWebhookApiAdapter(organizationFacadeAdapter, githubWebhookProperties, objectMapper);
    }


    @Data
    public static class OrganizationAdapterMock implements OrganizationFacadeAdapter {

        private final List<Organization> organizations = new ArrayList<>();

        @Override
        public Organization createOrganizationForVcsNameAndExternalId(String vcsOrganizationName, String externalId) throws CatleanException {
            final Organization organization =
                    Organization.builder().vcsConfiguration(VcsConfiguration.builder().organizationName(vcsOrganizationName).build()).name(vcsOrganizationName).externalId(externalId).build();
            organizations.add(organization);
            return organization;
        }
    }
}
