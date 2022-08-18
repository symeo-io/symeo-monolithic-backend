package io.symeo.monolithic.backend.bootstrap.configuration;

import io.symeo.monolithic.backend.infrastructure.aws.s3.adapter.AmazonS3Properties;
import io.symeo.monolithic.backend.infrastructure.aws.s3.adapter.AmazonS3RawStorageAdapter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static io.symeo.monolithic.backend.infrastructure.aws.s3.adapter.AmazonS3ClientFactory.getAmazonS3Client;

@Profile("aws")
@Configuration
public class AmazonS3Configuration {

    @Bean
    @ConfigurationProperties(value = "infrastructure.aws")
    public AmazonS3Properties amazonS3Properties() {
        return new AmazonS3Properties();
    }

    @Bean
    public AmazonS3RawStorageAdapter rawStorageAdapter(final AmazonS3Properties amazonS3Properties) {
        return new AmazonS3RawStorageAdapter(amazonS3Properties,
                getAmazonS3Client());
    }
}
