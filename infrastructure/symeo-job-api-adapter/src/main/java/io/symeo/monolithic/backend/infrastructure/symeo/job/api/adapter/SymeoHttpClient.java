package io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter;

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

    public void startJobForOrganizationId(UUID organizationId) {
        try {
            final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(
                            URI.create(symeoJobApiProperties.getUrl() + "api/v1/job/data-processing?organization_id=" + organizationId.toString())
                    )
                    .GET()
                    .headers(symeoJobApiProperties.getHeaderKey(), symeoJobApiProperties.getApiKey());
            final HttpResponse<byte[]> httpResponse;
            httpResponse = this.httpClient.send(requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofByteArray());
            final int statusCode = httpResponse.statusCode();
            LOGGER.info("Start job for organization id respond http status {}", statusCode);
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Failed to send http request to start job for organization id {}", organizationId, e);
        }

    }
}
