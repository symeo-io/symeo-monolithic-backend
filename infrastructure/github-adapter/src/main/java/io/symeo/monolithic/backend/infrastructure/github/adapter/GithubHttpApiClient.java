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
    public GithubRepositoryDTO[] getRepositoriesForVcsOrganizationName(final String vcsOrganizationName,
                                                                       final Integer page,
                                                                       final Integer size) throws SymeoException {
        final String uri =
                api
                        + "orgs/"
                        + encodeValue(vcsOrganizationName)
                        + "/repos?sort=name&per_page="
                        + size.toString()
                        + "&page="
                        + page.toString();
        return get(
                uri,
                vcsOrganizationName,
                GithubRepositoryDTO[].class);
    }

    @Override
    public GithubPullRequestDTO[] getPullRequestsForRepositoryAndVcsOrganizationOrderByDescDate(final String vcsOrganizationName,
                                                                                                final String repositoryName,
                                                                                                final Integer page,
                                                                                                final Integer size) throws SymeoException {
        final String uri =
                api
                        + "repos/"
                        + encodeValue(vcsOrganizationName)
                        + "/"
                        + encodeValue(repositoryName)
                        + "/pulls?sort=updated&direction=desc&state=all&per_page="
                        + size.toString()
                        + "&page="
                        + page.toString();
        return get(
                uri,
                vcsOrganizationName,
                GithubPullRequestDTO[].class);
    }

    @Override
    public GithubPullRequestDTO getPullRequestDetailsForPullRequestNumber(final String vcsOrganizationName,
                                                                          final String repositoryName,
                                                                          final Integer number) throws SymeoException {
        final String uri =
                api
                        + "repos/"
                        + encodeValue(vcsOrganizationName)
                        + "/"
                        + encodeValue(repositoryName)
                        + "/pulls/"
                        + number;
        return get(
                uri,
                vcsOrganizationName,
                GithubPullRequestDTO.class
        );
    }

    @Override
    public GithubCommitsDTO[] getCommitsForPullRequestNumber(final String vcsOrganizationName,
                                                             final String repositoryName,
                                                             final int pullRequestNumber,
                                                             final Integer page,
                                                             final Integer size) throws SymeoException {
        final String uri =
                api
                        + "repos/"
                        + encodeValue(vcsOrganizationName)
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
                vcsOrganizationName,
                GithubCommitsDTO[].class
        );
    }

    @Override
    public GithubCommentsDTO[] getCommentsForPullRequestNumber(final String vcsOrganizationName,
                                                               final String repositoryName,
                                                               final Integer pullRequestNumber,
                                                               final Integer page,
                                                               final Integer size) throws SymeoException {
        final String uri =
                api
                        + "repos/"
                        + encodeValue(vcsOrganizationName)
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
                vcsOrganizationName,
                GithubCommentsDTO[].class
        );
    }

    @Override
    public GithubBranchDTO[] getBranchesForVcsOrganizationAndRepository(final String vcsOrganizationName,
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
    public GithubCommitsDTO[] getCommitsForVcsOrganizationAndRepositoryAndBranchInDateRange(final String vcsOrganizationName,
                                                                                            final String repositoryName,
                                                                                            final String branchName,
                                                                                            final Date startDate,
                                                                                            final Date endDate,
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
                        new SimpleDateFormat(GITHUB_QUERY_DATE_FORMAT).format(startDate))
                        + String.format("&until=%s",
                        new SimpleDateFormat(GITHUB_QUERY_DATE_FORMAT).format(endDate))
                        + "&per_page="
                        + size.toString()
                        + "&page="
                        + page.toString()
                        + String.format("&sha=%s", encodeValue(branchName));
        return get(uri, vcsOrganizationName, GithubCommitsDTO[].class);
    }

    @Override
    public GithubCommitsDTO[] getCommitsForVcsOrganizationAndRepositoryAndBranch(String vcsOrganizationName,
                                                                                 String repositoryName,
                                                                                 String branchName, Integer page,
                                                                                 Integer size) throws SymeoException {
        String uri =
                api
                        + "repos/"
                        + encodeValue(vcsOrganizationName)
                        + "/"
                        + encodeValue(repositoryName)
                        + "/commits"
                        + "?per_page="
                        + size.toString()
                        + "&page="
                        + page.toString()
                        + String.format("&sha=%s", encodeValue(branchName));
        return get(uri, vcsOrganizationName, GithubCommitsDTO[].class);
    }

    @Override
    public GithubTagDTO[] getTagsForVcsOrganizationAndRepository(String vcsOrganizationName, String repositoryName) throws SymeoException {
        String uri =
                api
                        + "repos/"
                        + encodeValue(vcsOrganizationName)
                        + "/"
                        + encodeValue(repositoryName)
                        + "/git/matching-refs/tags";
        return get(uri, vcsOrganizationName, GithubTagDTO[].class);
    }

    private <ResponseBody> ResponseBody get(String uri, String vcsOrganizationName,
                                            Class<ResponseBody> responseClass) throws SymeoException {
        try {
            final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(uri))
                    .GET();
            getGithubAuthenticationHeader(vcsOrganizationName).forEach(requestBuilder::header);
            final HttpResponse<byte[]> httpResponse = this.httpClient.send(requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofByteArray());
            final int statusCode = httpResponse.statusCode();
            if (statusCode == 200) {
                return objectMapper.readValue(httpResponse.body(), responseClass);
            } else if (statusCode == 401) {
                flushGithubAuthenticationHeader(vcsOrganizationName);
                return get(uri, vcsOrganizationName, responseClass);
            }
            throw buildUnhandledHttpStatusCodeException(uri, vcsOrganizationName, statusCode);
        } catch (IOException | InterruptedException e) {
            throw buildErrorWhileExecutingHttpRequestException(uri, vcsOrganizationName);
        }
    }

    private void flushGithubAuthenticationHeader(final String vcsOrganizationName) {
        INSTALLATION_TOKEN_MAPPED_TO_ORGANIZATION.remove(vcsOrganizationName);
    }

    private Map<String, String> getGithubAuthenticationHeader(String vcsOrganizationName) throws SymeoException {
        if (!INSTALLATION_TOKEN_MAPPED_TO_ORGANIZATION.containsKey(vcsOrganizationName)) {
            INSTALLATION_TOKEN_MAPPED_TO_ORGANIZATION.put(vcsOrganizationName,
                    getGithubInstallationTokenForOrganization(vcsOrganizationName));
        }
        return Map.of(AUTHORIZATION_HEADER_KEY,
                AUTHORIZATION_HEADER_TOKEN_VALUE + INSTALLATION_TOKEN_MAPPED_TO_ORGANIZATION.get(vcsOrganizationName));
    }

    private String getGithubInstallationTokenForOrganization(String vcsOrganizationName) throws SymeoException {

        final String jwtTokenBearer = "Bearer " + githubJwtTokenProvider.generateSignedJwtToken();
        for (GithubInstallationDTO githubInstallationDTO : getGithubInstallationsDTO(jwtTokenBearer,
                vcsOrganizationName)) {
            if (githubInstallationDTO.getAccount().getLogin().equals(vcsOrganizationName)) {
                return getInstallationToken(vcsOrganizationName, jwtTokenBearer, githubInstallationDTO);
            }
        }
        throw buildOrgaTokenNotFoundException(vcsOrganizationName);

    }

    private String getInstallationToken(String vcsOrganizationName, String jwtTokenBearer,
                                        GithubInstallationDTO githubInstallationDTO) throws SymeoException {
        final HttpRequest.Builder requestBuilder = HttpRequest
                .newBuilder(URI.create(api + "app/installations/" + githubInstallationDTO.getId() +
                        "/access_tokens"))
                .POST(HttpRequest.BodyPublishers.noBody());

        Map.of(AUTHORIZATION_HEADER_KEY, jwtTokenBearer).forEach(requestBuilder::header);
        final GithubInstallationAccessTokenDTO githubInstallationAccessTokenDTO =
                sendRequest(requestBuilder.build(), GithubInstallationAccessTokenDTO.class, vcsOrganizationName);
        return githubInstallationAccessTokenDTO.getToken();
    }

    private GithubInstallationDTO[] getGithubInstallationsDTO(final String jwtTokenBearer,
                                                              final String vcsOrganizationName) throws SymeoException {
        try {
            final String uri =
                    api
                            + "app/installations";
            return sendRequest(HttpRequest.newBuilder(new URI(uri)).headers("Authorization",
                    jwtTokenBearer).build(), GithubInstallationDTO[].class, vcsOrganizationName);
        } catch (URISyntaxException e) {
            throw buildInvalidUriException(e);
        }
    }

    private <ResponseBody> ResponseBody sendRequest(final HttpRequest httpRequest,
                                                    final Class<ResponseBody> responseClass,
                                                    final String vcsOrganizationName) throws SymeoException {
        try {
            final HttpResponse<byte[]> httpResponse = this.httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofByteArray());
            if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() <= 299) {
                return objectMapper.readValue(httpResponse.body(), responseClass);
            }
            throw buildUnhandledHttpStatusCodeException(httpRequest.uri().toString(), vcsOrganizationName,
                    httpResponse.statusCode());
        } catch (final InterruptedException | IOException e) {
            throw buildErrorWhileExecutingHttpRequestException(httpRequest.uri().toString(), vcsOrganizationName);
        }
    }

    private static SymeoException buildErrorWhileExecutingHttpRequestException(String uri, String vcsOrganizationName) {
        final String message = String.format("Error while calling %s for organization %s", uri, vcsOrganizationName);
        LOGGER.error(message);
        return SymeoException.builder()
                .message(message)
                .code(ERROR_WHILE_EXECUTING_HTTP_REQUEST)
                .build();
    }

    private static SymeoException buildUnhandledHttpStatusCodeException(String uri, String vcsOrganizationName,
                                                                        int statusCode) {
        final String message = String.format("Http status %d not handle while calling uri %s for organization %s"
                , statusCode, uri, vcsOrganizationName);
        return SymeoException.builder()
                .code(UNHANDLED_HTTP_STATUS_CODE)
                .message(message)
                .build();
    }

    private static SymeoException buildOrgaTokenNotFoundException(String vcsOrganizationName) {
        final String message = String.format("Installation token not found for organization %s", vcsOrganizationName);
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
