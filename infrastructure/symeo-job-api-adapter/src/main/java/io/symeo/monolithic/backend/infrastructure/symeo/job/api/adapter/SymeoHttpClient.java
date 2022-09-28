package io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@AllArgsConstructor
@Slf4j
public class SymeoHttpClient {

    private final HttpClient httpClient;
    private final SymeoJobApiProperties symeoJobApiProperties;

    public void startJobForOrganizationId(UUID organizationId) throws SymeoException {
        try {
            final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(
                            URI.create(symeoJobApiProperties.getUrl() + "job/v1/data-processing/organization" +
                                    "?organization_id=" + organizationId.toString())
                    )
                    .GET()
                    .headers(symeoJobApiProperties.getHeaderKey(), symeoJobApiProperties.getApiKey());
            final HttpResponse<byte[]> httpResponse;
            httpResponse = this.httpClient.send(requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofByteArray());
            final int statusCode = httpResponse.statusCode();
            LOGGER.info("Start job for organization id respond http status {}", statusCode);
        } catch (IOException | InterruptedException e) {
            final String message = String.format("Failed to send http request to start job for organization id %s",
                    organizationId);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .code(SymeoExceptionCode.SYMEO_HTTP_CLIENT_ERROR)
                    .message(message)
                    .rootException(e)
                    .build();
        }

    }

    public void startJobForOrganizationIdAndTeamId(UUID organizationId, UUID teamId) throws SymeoException {
        try {
            final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(
                            URI.create(symeoJobApiProperties.getUrl() + "job/v1/data-processing/team?organization_id" +
                                    "=" + organizationId.toString()
                                    + "&team_id=" + teamId.toString())
                    )
                    .GET()
                    .headers(symeoJobApiProperties.getHeaderKey(), symeoJobApiProperties.getApiKey());
            final HttpResponse<byte[]> httpResponse;
            httpResponse = this.httpClient.send(requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofByteArray());
            final int statusCode = httpResponse.statusCode();
            LOGGER.info("Start job for organization id respond http status {}", statusCode);
        } catch (IOException | InterruptedException e) {
            final String message = String.format("Failed to send http request to start job for organization id %s and" +
                            " team id %s",
                    organizationId, teamId);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .code(SymeoExceptionCode.SYMEO_HTTP_CLIENT_ERROR)
                    .message(message)
                    .rootException(e)
                    .build();
        }
    }
}
