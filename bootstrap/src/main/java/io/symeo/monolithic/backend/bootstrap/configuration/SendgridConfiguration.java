package io.symeo.monolithic.backend.bootstrap.configuration;

import io.symeo.monolithic.backend.domain.bff.port.out.EmailDeliveryAdapter;
import io.symeo.monolithic.backend.infrastructure.sendgrid.adapter.SendgridAdapter;
import io.symeo.monolithic.backend.infrastructure.sendgrid.adapter.client.SendgridApiClient;
import io.symeo.monolithic.backend.infrastructure.sendgrid.adapter.properties.SendgridProperties;
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
