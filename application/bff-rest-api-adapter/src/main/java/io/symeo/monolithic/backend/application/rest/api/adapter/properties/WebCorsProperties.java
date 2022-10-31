package io.symeo.monolithic.backend.application.rest.api.adapter.properties;

import lombok.Data;

@Data
public class WebCorsProperties {
    private String[] hosts;
}
