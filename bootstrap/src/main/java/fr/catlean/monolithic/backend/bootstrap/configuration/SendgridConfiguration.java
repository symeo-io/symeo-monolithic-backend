package fr.catlean.monolithic.backend.bootstrap.configuration;

import fr.catlean.monolithic.backend.domain.port.out.EmailDeliveryAdapter;
import fr.catlean.monolithic.backend.infrastructure.sendgrid.adapter.SendgridAdapter;
import fr.catlean.monolithic.backend.infrastructure.sendgrid.adapter.client.SendgridApiClient;
import fr.catlean.monolithic.backend.infrastructure.sendgrid.adapter.properties.SendgridProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SendgridConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "infrastructure.sendgrid")
    public SendgridProperties sendgridProperties() {
        return new SendgridProperties();
    }

    @Bean
    public SendgridApiClient sendgridApiClient(final SendgridProperties sendgridProperties) {
        return new SendgridApiClient(sendgridProperties);
    }

    @Bean
    public EmailDeliveryAdapter emailDeliveryAdapter(final SendgridApiClient sendgridApiClient) {
        return new SendgridAdapter(sendgridApiClient);
    }
}
