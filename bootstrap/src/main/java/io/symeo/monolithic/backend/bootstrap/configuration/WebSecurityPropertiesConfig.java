package io.symeo.monolithic.backend.bootstrap.configuration;

import io.symeo.monolithic.backend.application.rest.api.adapter.authentication.Auth0SecurityProperties;
import io.symeo.monolithic.backend.application.rest.api.adapter.properties.WebCorsProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebSecurityPropertiesConfig {

    @Bean
    @ConfigurationProperties("application.web.cors")
    public WebCorsProperties webCorsProperties() {
        return new WebCorsProperties();
    }


    @Bean
    @ConfigurationProperties("application.auth0")
    public Auth0SecurityProperties auth0SecurityProperties() {
        return new Auth0SecurityProperties();
    }

}
