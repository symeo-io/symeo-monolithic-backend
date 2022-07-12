package fr.catlean.monolithic.backend.bootstrap.configuration;

import fr.catlean.monolithic.backend.bootstrap.authentication.Auth0SecurityConfiguration;
import fr.catlean.monolithic.backend.bootstrap.authentication.Auth0SecurityProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import(value = Auth0SecurityConfiguration.class)
public class Auth0Configuration {

    @Bean
    @ConfigurationProperties("auth0")
    public Auth0SecurityProperties auth0SecurityProperties() {
        return new Auth0SecurityProperties();
    }
}
