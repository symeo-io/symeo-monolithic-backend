package catlean.http.cient;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public interface CatleanHttpClient {

    <ResponseBody> ResponseBody get(final String uri, Class<ResponseBody> responseClass, ObjectMapper objectMapper,
                                    final Map<String, String> headers);

}
