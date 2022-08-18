package io.symeo.monolithic.backend.bootstrap.configuration;

import io.symeo.monolithic.backend.application.rest.api.adapter.properties.WebCorsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebCorsConfig implements WebMvcConfigurer {

    private final WebCorsProperties webCorsProperties;

    @Autowired
    public WebCorsConfig(WebCorsProperties webCorsProperties) {
        this.webCorsProperties = webCorsProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedMethods("GET", "PUT", "POST", "DELETE", "PATCH")
                .allowedOrigins(this.webCorsProperties.getHosts());
    }

}
