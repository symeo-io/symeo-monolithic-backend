package io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.dto.PostStartDataProcessingJobForOrganizationDTO;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.dto.PostStartDataProcessingJobForRepositoriesDTO;
import io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter.dto.PostStartDataProcessingJobForTeamDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@AllArgsConstructor
@Slf4j
public class SymeoHttpClient {

    private final HttpClient httpClient;
    private final SymeoDataProcessingJobApiProperties symeoDataProcessingJobApiProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";

    public void startDataProcessingJobForOrganizationIdAndRepositoryIds(final PostStartDataProcessingJobForRepositoriesDTO postStartDataProcessingJobForRepositoriesDTO) throws SymeoException {
        try {
            final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(
                            URI.create(symeoDataProcessingJobApiProperties.getUrl() + "job/v1/data-processing" +
                                    "/organization" +
                                    "/repositories"
                            ))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(dtoToBytes(postStartDataProcessingJobForRepositoriesDTO)))
                    .headers(symeoDataProcessingJobApiProperties.getHeaderKey(),
                            symeoDataProcessingJobApiProperties.getApiKey(), CONTENT_TYPE, APPLICATION_JSON);
            final HttpResponse<byte[]> httpResponse;
            httpResponse = this.httpClient.send(requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofByteArray());
            final int statusCode = httpResponse.statusCode();
            LOGGER.info("Start job for dto {} respond http status {}", postStartDataProcessingJobForRepositoriesDTO,
                    statusCode);
        } catch (IOException | InterruptedException e) {
            final String message = String.format("Failed to send http request to start job for dto %s",
                    postStartDataProcessingJobForRepositoriesDTO);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .code(SymeoExceptionCode.SYMEO_HTTP_CLIENT_ERROR)
                    .message(message)
                    .rootException(e)
                    .build();
        }
    }

    public void startDataProcessingJobForOrganizationIdAndTeamIdAndRepositoryIds(final PostStartDataProcessingJobForTeamDTO postStartDataProcessingJobForTeamDTO) throws SymeoException {
        try {
            final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(
                            URI.create(symeoDataProcessingJobApiProperties.getUrl() + "job/v1/data-processing" +
                                    "/organization" +
                                    "/team/repositories"
                            ))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(dtoToBytes(postStartDataProcessingJobForTeamDTO)))
                    .headers(symeoDataProcessingJobApiProperties.getHeaderKey(),
                            symeoDataProcessingJobApiProperties.getApiKey(), CONTENT_TYPE, APPLICATION_JSON);
            final HttpResponse<byte[]> httpResponse;
            httpResponse = this.httpClient.send(requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofByteArray());
            final int statusCode = httpResponse.statusCode();
            LOGGER.info("Start job for dto {} respond http status {}", postStartDataProcessingJobForTeamDTO,
                    statusCode);
        } catch (IOException | InterruptedException e) {
            final String message = String.format("Failed to send http request to start job for dto %s",
                    postStartDataProcessingJobForTeamDTO);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .code(SymeoExceptionCode.SYMEO_HTTP_CLIENT_ERROR)
                    .message(message)
                    .rootException(e)
                    .build();
        }
    }

    public void startDataProcessingJobForOrganizationIdAndVcsOrganizationId(final PostStartDataProcessingJobForOrganizationDTO postStartDataProcessingJobForOrganizationDTO) throws SymeoException {
        try {
            final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(
                            URI.create(symeoDataProcessingJobApiProperties.getUrl() + "job/v1/data-processing" +
                                    "/organization" +
                                    "/vcs_organization"
                            ))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(dtoToBytes(postStartDataProcessingJobForOrganizationDTO)))
                    .headers(
                            symeoDataProcessingJobApiProperties.getHeaderKey(),
                            symeoDataProcessingJobApiProperties.getApiKey(),
                            CONTENT_TYPE, APPLICATION_JSON
                    );
            final HttpResponse<byte[]> httpResponse;
            httpResponse = this.httpClient.send(requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofByteArray());
            final int statusCode = httpResponse.statusCode();
            LOGGER.info("Start job for dto {} respond http status {}", postStartDataProcessingJobForOrganizationDTO,
                    statusCode);
        } catch (IOException | InterruptedException e) {
            final String message = String.format("Failed to send http request to start job for dto %s",
                    postStartDataProcessingJobForOrganizationDTO);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .code(SymeoExceptionCode.SYMEO_HTTP_CLIENT_ERROR)
                    .message(message)
                    .rootException(e)
                    .build();
        }
    }

    public <DTO> byte[] dtoToBytes(DTO dto) throws SymeoException {
        try {
            return objectMapper.writeValueAsBytes(dto);
        } catch (JsonProcessingException e) {
            final String message = String.format("Failed to serialize symeoDataProcessingApiDTO %s", dto);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .message(message)
                    .code(SymeoExceptionCode.SYMEO_HTTP_CLIENT_ERROR)
                    .rootException(e)
                    .build();
        }
    }
}
