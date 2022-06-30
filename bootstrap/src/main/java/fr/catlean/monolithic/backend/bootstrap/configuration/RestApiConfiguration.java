package fr.catlean.monolithic.backend.bootstrap.configuration;

import catlean.monolithic.backend.rest.api.adapter.DataProcessingRestApiAdapter;
import catlean.monolithic.backend.rest.api.adapter.PullRequestRestApiAdapter;
import fr.catlean.monolithic.backend.domain.port.in.DataProcessingJobAdapter;
import org.springframework.context.annotation.Bean;

public class RestApiConfiguration {

    @Bean
    public DataProcessingRestApiAdapter dataProcessingJobApi(final DataProcessingJobAdapter dataProcessingJobAdapter) {
        return new DataProcessingRestApiAdapter(dataProcessingJobAdapter);
    }

    @Bean
    public PullRequestRestApiAdapter pullRequestRestApiAdapter() {
        return new PullRequestRestApiAdapter();
    }

}
