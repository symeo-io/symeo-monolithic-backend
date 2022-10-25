package io.symeo.monolithic.backend.infrastructure.github.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.github.adapter.jwt.GithubJwtTokenProvider;
import io.symeo.monolithic.backend.job.domain.github.dto.GithubBranchDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.GithubTagDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.installation.GithubInstallationAccessTokenDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.installation.GithubInstallationDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.pr.GithubCommentsDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.pr.GithubCommitsDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.pr.GithubPullRequestDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.repo.GithubRepositoryDTO;
import io.symeo.monolithic.backend.job.domain.port.out.GithubApiClientAdapter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.*;

@Slf4j
public class GithubHttpApiClient implements GithubApiClientAdapter {

    private static final String GITHUB_QUERY_DATE_FORMAT = "YYYY-MM-dd'T'HH:mm:ssZ";
    private static final String AUTHORIZATION_HEADER_KEY = "Authorization";
    private static final String AUTHORIZATION_HEADER_TOKEN_VALUE = "Bearer ";
    private static final Map<String, String> INSTALLATION_TOKEN_MAPPED_TO_ORGANIZATION = new HashMap<>();
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final GithubJwtTokenProvider githubJwtTokenProvider;
    private final String api;


    public GithubHttpApiClient(final ObjectMapper objectMapper, final HttpClient httpClient,
                               final GithubJwtTokenProvider githubJwtTokenProvider, final String api) {
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
        this.githubJwtTokenProvider = githubJwtTokenProvider;
        this.api = api;
    }

    @Override
    public GithubRepositoryDTO[] getRepositoriesForOrganizationName(final String organizationName,
                                                                    final Integer page,
                                                                    final Integer size) throws SymeoException {
        final String uri =
                api
                        + "orgs/"
                        + encodeValue(organizationName)
                        + "/repos?sort=name&per_page="
                        + size.toString()
                        + "&page="
                        + page.toString();
        return get(
                uri,
                organizationName,
                GithubRepositoryDTO[].class);
    }

    @Override
    public GithubPullRequestDTO[] getPullRequestsForRepositoryAndOrganizationOrderByDescDate(final String organizationName,
                                                                                             final String repositoryName,
                                                                                             final Integer page,
                                                                                             final Integer size) throws SymeoException {
        final String uri =
                api
                        + "repos/"
                        + encodeValue(organizationName)
                        + "/"
                        + encodeValue(repositoryName)
                        + "/pulls?sort=updated&direction=desc&state=all&per_page="
                        + size.toString()
                        + "&page="
                        + page.toString();
        return get(
                uri,
                organizationName,
                GithubPullRequestDTO[].class);
    }

    @Override
    public GithubPullRequestDTO getPullRequestDetailsForPullRequestNumber(final String organizationName,
                                                                          final String repositoryName,
                                                                          final Integer number) throws SymeoException {
        final String uri =
                api
                        + "repos/"
                        + encodeValue(organizationName)
                        + "/"
                        + encodeValue(repositoryName)
                        + "/pulls/"
                        + number;
        return get(
                uri,
                organizationName,
                GithubPullRequestDTO.class
        );
    }

    @Override
    public GithubCommitsDTO[] getCommitsForPullRequestNumber(final String organizationName,
                                                             final String repositoryName,
                                                             final int pullRequestNumber,
                                                             final Integer page,
                                                             final Integer size) throws SymeoException {
        final String uri =
                api
                        + "repos/"
                        + encodeValue(organizationName)
                        + "/"
                        + encodeValue(repositoryName)
                        + "/pulls/"
                        + pullRequestNumber
                        + "/commits?page="
                        + page.toString()
                        + "&per_page="
                        + size.toString();
        return get(
                uri,
                organizationName,
                GithubCommitsDTO[].class
        );
    }

    @Override
    public GithubCommentsDTO[] getCommentsForPullRequestNumber(final String organizationName,
                                                               final String repositoryName,
                                                               final Integer pullRequestNumber,
                                                               final Integer page,
                                                               final Integer size) throws SymeoException {
        final String uri =
                api
                        + "repos/"
                        + encodeValue(organizationName)
                        + "/"
                        + encodeValue(repositoryName)
                        + "/pulls/"
                        + pullRequestNumber
                        + "/comments?page="
                        + page.toString()
                        + "&per_page="
                        + size.toString();
        return get(
                uri,
                organizationName,
                GithubCommentsDTO[].class
        );
    }

    @Override
    public GithubBranchDTO[] getBranchesForOrganizationAndRepository(final String vcsOrganizationName,
                                                                     final String repositoryName,
                                                                     final Integer page,
                                                                     final Integer size) throws SymeoException {
        final String uri =
                api
                        + "repos/"
                        + encodeValue(vcsOrganizationName)
                        + "/"
                        + encodeValue(repositoryName)
                        + "/branches"
                        + "?per_page="
                        + size.toString()
                        + "&page="
                        + page.toString();
        return get(uri,
                vcsOrganizationName,
                GithubBranchDTO[].class);
    }

    @Override
    public GithubCommitsDTO[] getCommitsForOrganizationAndRepositoryAndBranchFromLastCollectionDate(final String vcsOrganizationName,
                                                                                                    final String repositoryName,
                                                                                                    final String branchName,
                                                                                                    final Date lastCollectionDate,
                                                                                                    final Integer page,
                                                                                                    final Integer size) throws SymeoException {
        String uri =
                api
                        + "repos/"
                        + encodeValue(vcsOrganizationName)
                        + "/"
                        + encodeValue(repositoryName)
                        + "/commits"
                        + String.format("?since=%s",
                        new SimpleDateFormat(GITHUB_QUERY_DATE_FORMAT).format(lastCollectionDate))
                        + "&per_page="
                        + size.toString()
                        + "&page="
                        + page.toString()
                        + String.format("&sha=%s", encodeValue(branchName));
        return get(uri, vcsOrganizationName, GithubCommitsDTO[].class);
    }

    @Override
    public GithubCommitsDTO[] getCommitsForOrganizationAndRepositoryAndBranch(final String vcsOrganizationName,
                                                                              final String repositoryName,
                                                                              final String branchName,
                                                                              final Integer page,
                                                                              final Integer size) throws SymeoException {
        String uri =
                api
                        + "repos/"
                        + encodeValue(vcsOrganizationName)
                        + "/"
                        + encodeValue(repositoryName)
                        + "/commits"
                        + String.format("?page=%s", page.toString())
                        + String.format("&per_page=%s", size.toString())
                        + String.format("&sha=%s", encodeValue(branchName));
        return get(uri, vcsOrganizationName, GithubCommitsDTO[].class);
    }

    @Override
    public GithubTagDTO[] getTagsForOrganizationAndRepository(String vcsOrganizationName, String repositoryName) throws SymeoException {
        String uri =
                api
                        + "repos/"
                        + encodeValue(vcsOrganizationName)
                        + "/"
                        + encodeValue(repositoryName)
                        + "/git/matching-refs/tags";
        return get(uri, vcsOrganizationName, GithubTagDTO[].class);
    }

    private <ResponseBody> ResponseBody get(String uri, String organizationName,
                                            Class<ResponseBody> responseClass) throws SymeoException {
        try {
            final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(uri))
                    .GET();
            getGithubAuthenticationHeader(organizationName).forEach(requestBuilder::header);
            final HttpResponse<byte[]> httpResponse = this.httpClient.send(requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofByteArray());
            final int statusCode = httpResponse.statusCode();
            if (statusCode == 200) {
                return objectMapper.readValue(httpResponse.body(), responseClass);
            } else if (statusCode == 401) {
                flushGithubAuthenticationHeader(organizationName);
                return get(uri, organizationName, responseClass);
            }
            throw buildUnhandledHttpStatusCodeException(uri, organizationName, statusCode);
        } catch (IOException | InterruptedException e) {
            throw buildErrorWhileExecutingHttpRequestException(uri, organizationName);
        }
    }

    private void flushGithubAuthenticationHeader(final String organizationName) {
        INSTALLATION_TOKEN_MAPPED_TO_ORGANIZATION.remove(organizationName);
    }

    private Map<String, String> getGithubAuthenticationHeader(String organizationName) throws SymeoException {
        if (!INSTALLATION_TOKEN_MAPPED_TO_ORGANIZATION.containsKey(organizationName)) {
            INSTALLATION_TOKEN_MAPPED_TO_ORGANIZATION.put(organizationName,
                    getGithubInstallationTokenForOrganization(organizationName));
        }
        return Map.of(AUTHORIZATION_HEADER_KEY,
                AUTHORIZATION_HEADER_TOKEN_VALUE + INSTALLATION_TOKEN_MAPPED_TO_ORGANIZATION.get(organizationName));
    }

    private String getGithubInstallationTokenForOrganization(String organizationName) throws SymeoException {

        final String jwtTokenBearer = "Bearer " + githubJwtTokenProvider.generateSignedJwtToken();
        for (GithubInstallationDTO githubInstallationDTO : getGithubInstallationsDTO(jwtTokenBearer,
                organizationName)) {
            if (githubInstallationDTO.getAccount().getLogin().equals(organizationName)) {
                return getInstallationToken(organizationName, jwtTokenBearer, githubInstallationDTO);
            }
        }
        throw buildOrgaTokenNotFoundException(organizationName);

    }

    private String getInstallationToken(String organizationName, String jwtTokenBearer,
                                        GithubInstallationDTO githubInstallationDTO) throws SymeoException {
        final HttpRequest.Builder requestBuilder = HttpRequest
                .newBuilder(URI.create(api + "app/installations/" + githubInstallationDTO.getId() +
                        "/access_tokens"))
                .POST(HttpRequest.BodyPublishers.noBody());

        Map.of(AUTHORIZATION_HEADER_KEY, jwtTokenBearer).forEach(requestBuilder::header);
        final GithubInstallationAccessTokenDTO githubInstallationAccessTokenDTO =
                sendRequest(requestBuilder.build(), GithubInstallationAccessTokenDTO.class, organizationName);
        return githubInstallationAccessTokenDTO.getToken();
    }

    private GithubInstallationDTO[] getGithubInstallationsDTO(final String jwtTokenBearer,
                                                              final String organizationName) throws SymeoException {
        try {
            final String uri =
                    api
                            + "app/installations";
            return sendRequest(HttpRequest.newBuilder(new URI(uri)).headers("Authorization",
                    jwtTokenBearer).build(), GithubInstallationDTO[].class, organizationName);
        } catch (URISyntaxException e) {
            throw buildInvalidUriException(e);
        }
    }

    private <ResponseBody> ResponseBody sendRequest(final HttpRequest httpRequest,
                                                    final Class<ResponseBody> responseClass,
                                                    final String organizationName) throws SymeoException {
        try {
            final HttpResponse<byte[]> httpResponse = this.httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofByteArray());
            if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() <= 299) {
                return objectMapper.readValue(httpResponse.body(), responseClass);
            }
            throw buildUnhandledHttpStatusCodeException(httpRequest.uri().toString(), organizationName,
                    httpResponse.statusCode());
        } catch (final InterruptedException | IOException e) {
            throw buildErrorWhileExecutingHttpRequestException(httpRequest.uri().toString(), organizationName);
        }
    }

    private static SymeoException buildErrorWhileExecutingHttpRequestException(String uri, String organizationName) {
        final String message = String.format("Error while calling %s for organization %s", uri, organizationName);
        LOGGER.error(message);
        return SymeoException.builder()
                .message(message)
                .code(ERROR_WHILE_EXECUTING_HTTP_REQUEST)
                .build();
    }

    private static SymeoException buildUnhandledHttpStatusCodeException(String uri, String organizationName,
                                                                        int statusCode) {
        final String message = String.format("Http status %d not handle while calling uri %s for organization %s"
                , statusCode, uri, organizationName);
        return SymeoException.builder()
                .code(UNHANDLED_HTTP_STATUS_CODE)
                .message(message)
                .build();
    }

    private static SymeoException buildOrgaTokenNotFoundException(String organizationName) {
        final String message = String.format("Installation token not found for organization %s", organizationName);
        LOGGER.error(message);
        return SymeoException.builder()
                .code(GITHUB_ORG_TOKEN_NOT_FOUND)
                .message(message)
                .build();
    }

    private static SymeoException buildInvalidUriException(URISyntaxException e) {
        final String message = "Invalid uri for github app installations";
        LOGGER.error(message, e);
        return SymeoException.builder()
                .code(INVALID_URI_FOR_GITHUB)
                .rootException(e)
                .message(message)
                .build();
    }

    private static String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

}
