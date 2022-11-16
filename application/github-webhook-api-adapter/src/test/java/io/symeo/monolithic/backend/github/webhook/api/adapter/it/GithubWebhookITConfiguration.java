package io.symeo.monolithic.backend.github.webhook.api.adapter.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.port.in.OrganizationFacadeAdapter;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.github.webhook.api.adapter.GithubWebhookApiAdapter;
import io.symeo.monolithic.backend.github.webhook.api.adapter.properties.GithubWebhookProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        public Organization createOrganization(final Organization organization) throws SymeoException {
            organizations.add(organization);
            return organization;
        }
        @Override
        public Optional<Organization> getOrganizationForApiKey(String key) throws SymeoException {
            return Optional.of(Organization.builder().build());
        }
    }
}
