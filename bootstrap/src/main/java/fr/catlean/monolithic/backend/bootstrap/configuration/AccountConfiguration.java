package fr.catlean.monolithic.backend.bootstrap.configuration;

import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountConfiguration {

//    @Bean
//    public OrganizationStorageAdapter organizationAccountAdapter() {
//        return new OrganizationStorageAdapter() {
//            private final static Map<String, Organization> ORGANISATION_ACCOUNT_MAP = new HashMap<>();
//
//            static {
//                Organization armisOrganizationAccount =
//                        Organization.builder().name("armis").vcsConfiguration(VcsConfiguration.builder().organizationName("armis-paris").build()).build()
//                                .addTeam("saas-team", List.of("Reference_API",
//                                        "saas-backend",
//                                        "saas-frontend",
//                                        "design-system",
//                                        "reference-consumer",
//                                        "Consolidation-API",
//                                        "facteur",
//                                        "business-monitor",
//                                        "spider",
//                                        "feedloader",
//                                        "account-api",
//                                        "account-api-contract",
//                                        "saas-bff",
//                                        "armis-context",
//                                        "armis-authenticator",
//                                        "maven-basement",
//                                        "arrow-addon",
//                                        "kotlin-utils"), 1000, 5)
//                                .addTeam("data-team", List.of(
//                                        "predictive-analytics",
//                                        "predictive-analytics-api",
//                                        "flai-away",
//                                        "bi-aggregator",
//                                        "AppnexusLLD_Fetch",
//                                        "armis-dtlk-raw-to-clean-lambda",
//                                        "infrastructure-datalake-core",
//                                        "datalake-data-ingestion"
//                                ), 500, 5)
//                                .addTeam("media-team", List.of("Media-API", "Media-Console", "armis-orchestrator"),
//                                        1000, 5);
//                Organization dalmaOrganizationAccount =
//                        Organization.builder().name("dalma").vcsConfiguration(VcsConfiguration.builder().organizationName("dalmaTeam").build()).build()
//                                .addTeam("front", List.of("web-reactjs", "marketing", "subscription-flow"), 500, 3)
//                                .addTeam("back", List.of("dalma-services"), 500, 3);
//                ORGANISATION_ACCOUNT_MAP.put("armis", armisOrganizationAccount);
//                ORGANISATION_ACCOUNT_MAP.put("dalma", dalmaOrganizationAccount);
//            }
//
//            @Override
//            public Organization findOrganizationForName(String organizationName) throws CatleanException {
//                if (ORGANISATION_ACCOUNT_MAP.containsKey(organizationName)) {
//                    return ORGANISATION_ACCOUNT_MAP.get(organizationName);
//                }
//                throw CatleanException.builder().code("F.ORGANIZATION_NOT_FOUND").message(String.format("Organization" +
//                        " %s not found", organizationName)).build();
//
//            }
//        };
//    }
}
