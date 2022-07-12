package fr.catlean.monolithic.backend.bootstrap.configuration;

import fr.catlean.monolithic.backend.bootstrap.authentication.Auth0SecurityConfiguration;
import fr.catlean.monolithic.backend.bootstrap.authentication.Auth0SecurityProperties;
import fr.catlean.monolithic.backend.bootstrap.cors.WebCorsConfig;
import fr.catlean.monolithic.backend.bootstrap.cors.WebCorsProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import(value = {Auth0SecurityConfiguration.class, WebCorsConfig.class})
public class WebSecurityConfiguration {

    @Bean
    @ConfigurationProperties("auth0")
    public Auth0SecurityProperties auth0SecurityProperties() {
        return new Auth0SecurityProperties();
    }

    @Bean
    @ConfigurationProperties("web.cors")
    public WebCorsProperties webCorsProperties() {
        return new WebCorsProperties();
    }
}
