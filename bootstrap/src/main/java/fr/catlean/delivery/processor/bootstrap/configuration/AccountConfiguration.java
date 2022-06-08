package fr.catlean.delivery.processor.bootstrap.configuration;

import fr.catlean.delivery.processor.domain.model.account.OrganisationAccount;
import fr.catlean.delivery.processor.domain.model.account.VcsConfiguration;
import fr.catlean.delivery.processor.domain.port.out.OrganisationAccountAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class AccountConfiguration {

    @Bean
    public OrganisationAccountAdapter organisationAccountAdapter() {
        return new OrganisationAccountAdapter() {

            private final static Map<String, OrganisationAccount> ORGANISATION_ACCOUNT_MAP =
                    Map.of("armis",
                            OrganisationAccount.builder().name("armis").vcsConfiguration(VcsConfiguration.builder().organisationName("armis-paris").build()).build(),
                            "dalma",
                            OrganisationAccount.builder().name("dalma").vcsConfiguration(VcsConfiguration.builder().organisationName("dalmaTeam").build()).build());

            @Override
            public OrganisationAccount findOrganisationForName(String organisationName) {
                return ORGANISATION_ACCOUNT_MAP.get(organisationName);
            }
        };
    }
}
