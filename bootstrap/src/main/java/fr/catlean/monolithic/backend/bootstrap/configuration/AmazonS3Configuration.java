package fr.catlean.monolithic.backend.bootstrap.configuration;

import fr.catlean.monolithic.backend.infrastructure.aws.s3.adapter.AmazonS3Properties;
import fr.catlean.monolithic.backend.infrastructure.aws.s3.adapter.AmazonS3RawStorageAdapter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static fr.catlean.monolithic.backend.infrastructure.aws.s3.adapter.AmazonS3ClientFactory.getAmazonS3Client;

@Profile("!local")
@Configuration
public class AmazonS3Configuration {

    @Bean
    @ConfigurationProperties(value = "aws")
    public AmazonS3Properties amazonS3Properties() {
        return new AmazonS3Properties();
    }

    @Bean
    public AmazonS3RawStorageAdapter rawStorageAdapter(final AmazonS3Properties amazonS3Properties) {
        return new AmazonS3RawStorageAdapter(amazonS3Properties,
                getAmazonS3Client());
    }
}
