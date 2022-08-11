package fr.catlean.monolithic.backend.bootstrap.configuration;

import fr.catlean.monolithic.backend.infrastructure.json.local.storage.JsonLocalStorageAdapter;
import fr.catlean.monolithic.backend.infrastructure.json.local.storage.properties.JsonStorageProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"local", "it"})
public class JsonLocalStorageConfiguration {

    @Bean
    @ConfigurationProperties("infrastructure.json-local-storage")
    public JsonStorageProperties jsonStorageProperties() {
        return new JsonStorageProperties();
    }

    @Bean
    public JsonLocalStorageAdapter jsonLocalStorageAdapter(
            JsonStorageProperties jsonStorageProperties) {
        return new JsonLocalStorageAdapter(jsonStorageProperties);
    }
}
