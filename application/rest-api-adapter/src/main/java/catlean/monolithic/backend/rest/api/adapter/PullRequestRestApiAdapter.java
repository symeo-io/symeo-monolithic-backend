package catlean.monolithic.backend.rest.api.adapter;

import catlean.monolithic.backend.rest.api.adapter.mapper.PullRequestHistogramContractMapper;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.query.HistogramQuery;
import fr.catlean.monolithic.backend.frontend.contract.api.PullRequestApi;
import fr.catlean.monolithic.backend.frontend.contract.api.model.HistogramResponseContract;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Tags(@Tag(name = "PullRequest"))
@AllArgsConstructor
public class PullRequestRestApiAdapter implements PullRequestApi {

    private final HistogramQuery histogramQuery;

    @Override
    public ResponseEntity<HistogramResponseContract> getPullRequestHistogram(String organizationName, String teamName
            , String histogramType) {
        try {
            return PullRequestHistogramContractMapper.domainToContract(histogramQuery.readPullRequestHistogram(organizationName, teamName, histogramType));
        } catch (CatleanException e) {
            return PullRequestHistogramContractMapper.errorToContract(e);
        }
    }
}
