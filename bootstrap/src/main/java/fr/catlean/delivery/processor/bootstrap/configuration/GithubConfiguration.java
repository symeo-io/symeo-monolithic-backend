package fr.catlean.delivery.processor.bootstrap.configuration;

import catlean.http.cient.DefaultCatleanHttpClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.catlean.delivery.processor.infrastructure.github.adapter.GithubAdapter;
import fr.catlean.delivery.processor.infrastructure.github.adapter.client.GithubHttpClient;
import fr.catlean.delivery.processor.infrastructure.github.adapter.properties.GithubProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
public class GithubConfiguration {

  @Bean
  @ConfigurationProperties("github")
  public GithubProperties githubProperties() {
    return new GithubProperties();
  }

  @Bean
  public HttpClient httpClient() {
    return HttpClient.newHttpClient();
  }

  @Bean
  public GithubHttpClient githubHttpClient(HttpClient httpClient) {
    return new GithubHttpClient(new DefaultCatleanHttpClient(httpClient), new ObjectMapper());
  }

  @Bean
  public GithubAdapter githubAdapter(
      GithubHttpClient githubHttpClient, GithubProperties githubProperties) {
    return new GithubAdapter(githubHttpClient, githubProperties);
  }
}
