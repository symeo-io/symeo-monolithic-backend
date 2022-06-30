package fr.catlean.monolithic.backend.bootstrap.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(
                        new ApiInfoBuilder()
                                .title("Catlean Monolithic Backend REST API")
                                .version("1.0.0-SNAPSHOT")
                                .contact(
                                        new Contact(
                                                "Pierre Oucif",
                                                "http://www.catlean.io",
                                                "pierre.oucif@catlean.fr"
                                        )
                                ).build()
                )
                .select()
                .apis(RequestHandlerSelectors.basePackage("catlean.monolithic.backend.rest.api.adapter"))
                .paths(PathSelectors.regex("/api/.*"))
                .build()
                .pathMapping("/");
    }
}
