package catlean.monolithic.backend.rest.api.adapter;

import fr.catlean.monolithic.backend.frontend.contract.api.PullRequestApi;
import fr.catlean.monolithic.backend.frontend.contract.api.model.HistogramResponseContract;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Tags(@Tag(name = "PullRequest"))
public class PullRequestRestApiAdapter implements PullRequestApi {

    @Override
    public ResponseEntity<HistogramResponseContract> getPullRequestHistogram() {
        return PullRequestApi.super.getPullRequestHistogram();
    }
}