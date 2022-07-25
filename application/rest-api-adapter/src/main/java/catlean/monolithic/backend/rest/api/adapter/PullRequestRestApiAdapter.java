package catlean.monolithic.backend.rest.api.adapter;

import catlean.monolithic.backend.rest.api.adapter.authentication.AuthenticationService;
import catlean.monolithic.backend.rest.api.adapter.mapper.PullRequestHistogramContractMapper;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.query.HistogramQuery;
import fr.catlean.monolithic.backend.frontend.contract.api.PullRequestApi;
import fr.catlean.monolithic.backend.frontend.contract.api.model.HistogramResponseContract;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static catlean.monolithic.backend.rest.api.adapter.validator.PullRequestHistogramValidator.validate;


@RestController
@Tags(@Tag(name = "PullRequest"))
@AllArgsConstructor
public class PullRequestRestApiAdapter implements PullRequestApi {

    private final HistogramQuery histogramQuery;
    private final AuthenticationService authenticationService;

    @Override
    public ResponseEntity<HistogramResponseContract> getPullRequestHistogram(String teamName, String histogramType) {
        try {
            validate(histogramType);
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return PullRequestHistogramContractMapper.domainToContract(histogramQuery.readPullRequestHistogram(authenticatedUser.getOrganization(), teamName, histogramType));
        } catch (CatleanException e) {
            return PullRequestHistogramContractMapper.errorToContract(e);
        }
    }


}
