package fr.catlean.monolithic.backend.bootstrap.configuration;

import catlean.monolithic.backend.rest.api.adapter.authentication.Auth0SecurityProperties;
import fr.catlean.monolithic.backend.bootstrap.cors.WebCorsProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

public class WebSecurityConfiguration {

    @Bean
    @ConfigurationProperties("application.auth0")
    public Auth0SecurityProperties auth0SecurityProperties() {
        return new Auth0SecurityProperties();
    }

    @Bean
    @ConfigurationProperties("application.web.cors")
    public WebCorsProperties webCorsProperties() {
        return new WebCorsProperties();
    }
}
