package fr.catlean.monolithic.backend.bootstrap.configuration;

import catlean.monolithic.backend.rest.api.adapter.authentication.Auth0ContextProvider;
import catlean.monolithic.backend.rest.api.adapter.authentication.Auth0SecurityProperties;
import catlean.monolithic.backend.rest.api.adapter.authentication.AuthenticationContextProvider;
import com.auth0.spring.security.api.JwtWebSecurityConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@Profile("!it")
public class Auth0SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final Auth0SecurityProperties auth0SecurityProperties;

    @Autowired
    public Auth0SecurityConfiguration(Auth0SecurityProperties auth0SecurityProperties) {
        this.auth0SecurityProperties = auth0SecurityProperties;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        JwtWebSecurityConfigurer
                .forRS256(auth0SecurityProperties.getApiAudience(), auth0SecurityProperties.getApiIssuer())
                .configure(http);
        http.cors().and().authorizeRequests()
                .antMatchers("/api/**").authenticated();

    }

    @Bean
    @ConfigurationProperties("application.auth0")
    public Auth0SecurityProperties auth0SecurityProperties() {
        return new Auth0SecurityProperties();
    }


    @Bean
    public AuthenticationContextProvider authenticationContextProvider() {
        return new Auth0ContextProvider();
    }
}
