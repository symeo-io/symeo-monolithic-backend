package fr.catlean.delivery.processor.infrastructure.github.adapter.client;

import catlean.http.cient.CatleanHttpClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.catlean.delivery.processor.infrastructure.github.adapter.dto.pr.GithubPullRequestDTO;
import fr.catlean.delivery.processor.infrastructure.github.adapter.dto.repo.GithubRepositoryDTO;

import java.io.IOException;
import java.util.Map;

public class GithubHttpClient {

    private final CatleanHttpClient catleanHttpClient;
    private final ObjectMapper objectMapper;
    private final String githubApiBaseUrl = "https://api.github.com/";
    private final String authorizationHeaderKey = "Authorization";
    private final String authorizationHeaderTokenValue = "token ";
    private final String token;

    public GithubHttpClient(CatleanHttpClient catleanHttpClient, ObjectMapper objectMapper, String token) {
        this.catleanHttpClient = catleanHttpClient;
        this.objectMapper = objectMapper;
        this.token = token;
    }

    public GithubRepositoryDTO[] getRepositoriesForOrganizationName(
            String organizationName, Integer page, Integer size) {
        final String uri =
                githubApiBaseUrl
                        + "orgs/"
                        + organizationName
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

    public <T> byte[] dtoToBytes(T t) throws JsonProcessingException {
        return objectMapper.writeValueAsBytes(t);
    }

    public <T> T bytesToDto(byte[] bytes, Class<T> tClass) throws IOException {
        return objectMapper.readValue(bytes, tClass);
    }

    public GithubPullRequestDTO[] getPullRequestsForRepositoryAndOrganization(String organizationName,
                                                                              String repositoryName, Integer page,
                                                                              Integer size) {
        final String uri =
                githubApiBaseUrl
                        + "repos/"
                        + organizationName
                        + "/" +
                        repositoryName
                        + "/pulls?sort=updated&direction=desc&state=all&per_page="
                        + size.toString()
                        + "&page="
                        + page.toString();
        return this.catleanHttpClient.get(
                uri,
                GithubPullRequestDTO[].class,
                objectMapper,
                Map.of(authorizationHeaderKey, authorizationHeaderTokenValue + token));
    }

    public GithubPullRequestDTO getPullRequestDetailsForPullRequestNumber(final String organizationName,
                                                                          final String repositoryName,
                                                                          final Integer number) {
        final String uri =
                githubApiBaseUrl
                        + "repos/"
                        + organizationName
                        + "/" +
                        repositoryName
                        + "/pulls/"
                        + number;
        return this.catleanHttpClient.get(
                uri,
                GithubPullRequestDTO.class,
                objectMapper,
                Map.of(authorizationHeaderKey, authorizationHeaderTokenValue + token)
        );
    }
}
