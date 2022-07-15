package fr.catlean.monolithic.backend.bootstrap.configuration;

import catlean.monolithic.backend.rest.api.adapter.DataProcessingRestApiAdapter;
import catlean.monolithic.backend.rest.api.adapter.PullRequestRestApiAdapter;
import catlean.monolithic.backend.rest.api.adapter.UserRestApiAdapter;
import catlean.monolithic.backend.rest.api.adapter.authentication.AuthenticationService;
import fr.catlean.monolithic.backend.domain.port.in.DataProcessingJobAdapter;
import fr.catlean.monolithic.backend.domain.port.in.UserFacadeAdapter;
import fr.catlean.monolithic.backend.domain.query.HistogramQuery;
import org.springframework.context.annotation.Bean;

public class RestApiConfiguration {

    @Bean
    public DataProcessingRestApiAdapter dataProcessingJobApi(final DataProcessingJobAdapter dataProcessingJobAdapter) {
        return new DataProcessingRestApiAdapter(dataProcessingJobAdapter);
    }

    @Bean
    public PullRequestRestApiAdapter pullRequestRestApiAdapter(final HistogramQuery histogramQuery) {
        return new PullRequestRestApiAdapter(histogramQuery);
    }

    @Bean
    public UserRestApiAdapter userRestApiAdapter(final AuthenticationService authenticationService) {
        return new UserRestApiAdapter(authenticationService);
    }

    @Bean
    public AuthenticationService userAuthenticationService(final UserFacadeAdapter userFacadeAdapter) {
        return new AuthenticationService(userFacadeAdapter);
    }
}
