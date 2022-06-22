package fr.catlean.http.cient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public class DefaultCatleanHttpClient implements CatleanHttpClient {

    private final HttpClient httpClient;

    public DefaultCatleanHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public <RequestBody, ResponseBody> ResponseBody post(final String uri, final Optional<RequestBody> requestBody,
                                                         final Class<ResponseBody> responseClass,
                                                         final ObjectMapper objectMapper) {
        return post(uri, requestBody, responseClass, objectMapper, new HashMap<>());
    }

    @Override
    public <RequestBody, ResponseBody> ResponseBody post(String uri, Optional<RequestBody> requestBody,
                                                         Class<ResponseBody> responseClass, ObjectMapper objectMapper
            , Map<String, String> headers) {
        try {
            return sendRequest(getHttpPostRequest(uri, requestBody, objectMapper, headers), objectMapper,
                    responseClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unexpected error while building HttpRequest", e);
        }
    }

    @Override
    public <ResponseBody> ResponseBody get(String uri, Class<ResponseBody> responseClass,
                                           ObjectMapper objectMapper, Map<String, String> headers) {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(uri))
                .GET();
        if (nonNull(headers) && !headers.isEmpty()) {
            headers.forEach(requestBuilder::header);
        }
        return sendRequest(requestBuilder.build(), objectMapper, responseClass);
    }


    @SuppressWarnings("java:S2142") // TODO define if we want to deal with this here
    private <ResponseBody> ResponseBody sendRequest(final HttpRequest httpRequest, final ObjectMapper objectMapper,
                                                    Class<ResponseBody> responseClass) {
        try {
            final HttpResponse<byte[]> httpResponse = this.httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofByteArray());
            if (httpResponse.statusCode() == 200) {
                return objectMapper.readValue(httpResponse.body(), responseClass);
            }
            // TODO : finalize unit test with new error code
            throw new RuntimeException(
                    "Error while calling " + httpRequest + " : response " + httpResponse); // TODO Change this when
            // proper exception
        } catch (final JsonProcessingException e) {
            throw new RuntimeException("Unexpected error while processing HTTP request Body", e);
        } catch (final InterruptedException | IOException e) {
            throw new RuntimeException("Unexpected error while processing HTTP request", e);
        }
    }

    private <RequestBody> HttpRequest getHttpPostRequest(String uri, Optional<RequestBody> requestBody,
                                                         ObjectMapper objectMapper, Map<String, String> headers) throws JsonProcessingException {
        Optional<String> bodyAsString =
                requestBody.isPresent() ? of(objectMapper.writeValueAsString(requestBody.get())) : empty();
        final HttpRequest.Builder requestBuilder = HttpRequest
                .newBuilder(URI.create(uri))
                .POST(
                        bodyAsString
                                .map(HttpRequest.BodyPublishers::ofString).orElse(HttpRequest.BodyPublishers.noBody()));
        if (nonNull(headers) && !headers.isEmpty()) {
            headers.forEach(requestBuilder::header);
        }
        return requestBuilder.build();
    }
}
