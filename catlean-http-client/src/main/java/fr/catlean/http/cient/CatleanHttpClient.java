package fr.catlean.http.cient;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Optional;

public interface CatleanHttpClient {

    <RequestBody, ResponseBody> ResponseBody post(
            String uri,
            Optional<RequestBody> requestBody,
            Class<ResponseBody> responseClass,
            ObjectMapper objectMapper);

    <RequestBody, ResponseBody> ResponseBody post(
            String uri,
            Optional<RequestBody> requestBody,
            Class<ResponseBody> responseClass,
            ObjectMapper objectMapper,
            Map<String, String> headers);

    <ResponseBody> ResponseBody get(
            final String uri,
            Class<ResponseBody> responseClass,
            ObjectMapper objectMapper,
            final Map<String, String> headers);

    <ResponseBody> ResponseBody getWithRetries(
            final String uri,
            Class<ResponseBody> responseClass,
            ObjectMapper objectMapper,
            final Map<String, String> headers, int retryNumber);


}
