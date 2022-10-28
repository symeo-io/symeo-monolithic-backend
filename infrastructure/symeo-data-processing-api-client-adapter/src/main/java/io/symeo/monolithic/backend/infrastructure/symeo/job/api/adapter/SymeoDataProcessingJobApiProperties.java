package io.symeo.monolithic.backend.infrastructure.symeo.job.api.adapter;

import lombok.Data;

@Data
public class SymeoDataProcessingJobApiProperties {
    String apiKey;
    String url;
    String headerKey;
}
