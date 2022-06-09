package fr.catlean.delivery.processor.bootstrap.configuration;

import fr.catlean.delivery.processor.domain.model.account.OrganisationAccount;
import fr.catlean.delivery.processor.domain.model.account.VcsConfiguration;
import fr.catlean.delivery.processor.domain.port.out.OrganisationAccountAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class AccountConfiguration {

    @Bean
    public OrganisationAccountAdapter organisationAccountAdapter() {
        return new OrganisationAccountAdapter() {
            private final static Map<String, OrganisationAccount> ORGANISATION_ACCOUNT_MAP = new HashMap<>();

            static {
                OrganisationAccount armisOrganisationAccount =
                        OrganisationAccount.builder().name("armis").vcsConfiguration(VcsConfiguration.builder().organisationName("armis-paris").build()).build()
                                .addTeam("saas-team", List.of("Reference_API",
                                        "saas-backend",
                                        "saas-frontend",
                                        "design-system",
                                        "reference-consumer",
                                        "Consolidation-API",
                                        "facteur",
                                        "business-monitor",
                                        "spider",
                                        "feedloader",
                                        "account-api",
                                        "account-api-contract",
                                        "saas-bff",
                                        "armis-context",
                                        "armis-authenticator",
                                        "maven-basement",
                                        "arrow-addon",
                                        "kotlin-utils"), 1000, 5)
                                .addTeam("data-team", List.of(
                                        "predictive-analytics",
                                        "predictive-analytics-api",
                                        "flai-away",
                                        "bi-aggregator",
                                        "AppnexusLLD_Fetch",
                                        "armis-dtlk-raw-to-clean-lambda",
                                        "infrastructure-datalake-core",
                                        "datalake-data-ingestion"
                                ), 500, 5)
                                .addTeam("media-team", List.of("Media-API", "Media-Console", "armis-orchestrator"),
                                        1000, 5);
                OrganisationAccount dalmaOrganisationAccount =
                        OrganisationAccount.builder().name("dalma").vcsConfiguration(VcsConfiguration.builder().organisationName("dalmaTeam").build()).build()
                                .addTeam("front", List.of("web-reactjs", "marketing", "subscription-flow"), 3, 500)
                                .addTeam("back", List.of("dalma-services"), 3, 500);
                ORGANISATION_ACCOUNT_MAP.put("armis", armisOrganisationAccount);
                ORGANISATION_ACCOUNT_MAP.put("dalma", dalmaOrganisationAccount);
            }

            @Override
            public OrganisationAccount findOrganisationForName(String organisationName) {
                return ORGANISATION_ACCOUNT_MAP.get(organisationName);
            }
        };
    }
}
