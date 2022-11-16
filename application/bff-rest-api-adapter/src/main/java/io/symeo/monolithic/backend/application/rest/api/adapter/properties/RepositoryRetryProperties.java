package io.symeo.monolithic.backend.application.rest.api.adapter.properties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RepositoryRetryProperties {
    private Integer maxRetryNumber;
    private Integer retryTimeDelayInMillis;
}
