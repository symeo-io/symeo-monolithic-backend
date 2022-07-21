package catlean.monolithic.backend.rest.api.adapter.properties;

import lombok.Data;

@Data
public class WebCorsProperties {
    private String[] hosts;
}
