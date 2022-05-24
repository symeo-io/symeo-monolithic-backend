package fr.catlean.delivery.processor.bootstrap.configuration;

import fr.catlean.delivery.processor.infrastructure.json.local.storage.JsonLocalStorageAdapter;
import fr.catlean.delivery.processor.infrastructure.json.local.storage.properties.JsonStorageProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonLocalStorageConfiguration {

  @Bean
  @ConfigurationProperties("json-local-storage")
  public JsonStorageProperties jsonStorageProperties() {
    return new JsonStorageProperties();
  }

  @Bean
  public JsonLocalStorageAdapter jsonLocalStorageAdapter(
      JsonStorageProperties jsonStorageProperties) {
    return new JsonLocalStorageAdapter(jsonStorageProperties);
  }
}
