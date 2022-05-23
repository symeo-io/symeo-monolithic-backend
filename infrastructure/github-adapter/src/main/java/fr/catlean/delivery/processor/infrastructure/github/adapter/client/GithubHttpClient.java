package fr.catlean.delivery.processor.infrastructure.github.adapter.client;

import catlean.http.cient.CatleanHttpClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.catlean.delivery.processor.infrastructure.github.adapter.dto.GithubRepositoryDTO;

import java.util.Map;

public class GithubHttpClient {

  private final CatleanHttpClient catleanHttpClient;
  private final ObjectMapper objectMapper;
  private final String githubApiBaseUrl = "https://api.github.com/";
  private final String authorizationHeaderKey = "Authorization";
  private final String authorizationHeaderTokenValue = "token ";
  private final String token;

  public GithubHttpClient(
      CatleanHttpClient catleanHttpClient, ObjectMapper objectMapper, String token) {
    this.catleanHttpClient = catleanHttpClient;
    this.objectMapper = objectMapper;
    this.token = token;
  }

  public GithubRepositoryDTO[] getRepositoriesForOrganisationName(
      String organisationName, Integer page, Integer size) {
    final String uri = githubApiBaseUrl
            + "orgs/"
            + organisationName
            + "/repos?sort=name&per_page="
            + size.toString()
            + "&page="
            + page.toString();
    return this.catleanHttpClient.get(
            uri,
        GithubRepositoryDTO[].class,
        objectMapper,
        Map.of(authorizationHeaderKey, authorizationHeaderTokenValue + token));
  }
}
