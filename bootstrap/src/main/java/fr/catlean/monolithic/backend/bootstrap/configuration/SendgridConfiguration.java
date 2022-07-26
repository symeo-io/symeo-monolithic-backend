package fr.catlean.monolithic.backend.bootstrap.configuration;

import fr.catlean.monolithic.backend.domain.port.out.EmailDeliveryAdapter;
import fr.catlean.monolithic.backend.infrastructure.sendgrid.adapter.SendgridAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SendgridConfiguration {

    @Bean
    public EmailDeliveryAdapter emailDeliveryAdapter() {
        return new SendgridAdapter();
    }
}
