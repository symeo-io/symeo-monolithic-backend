package fr.catlean.monolithic.backend.bootstrap.configuration;

import catlean.monolithic.backend.rest.api.adapter.DataProcessingRestApiAdapter;
import fr.catlean.monolithic.backend.domain.port.in.DataProcessingJobAdapter;
import org.springframework.context.annotation.Bean;

public class DataProcessingJobRestApiConfiguration {

    @Bean
    public DataProcessingRestApiAdapter dataProcessingJobApi(final DataProcessingJobAdapter dataProcessingJobAdapter) {
        return new DataProcessingRestApiAdapter(dataProcessingJobAdapter);
    }

}
