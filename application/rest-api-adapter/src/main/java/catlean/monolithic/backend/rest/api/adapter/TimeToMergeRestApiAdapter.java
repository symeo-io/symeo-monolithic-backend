package catlean.monolithic.backend.rest.api.adapter;

import catlean.monolithic.backend.rest.api.adapter.authentication.AuthenticationService;
import catlean.monolithic.backend.rest.api.adapter.mapper.TimeToMergeCurveMapper;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.query.CurveQuery;
import fr.catlean.monolithic.backend.domain.query.HistogramQuery;
import fr.catlean.monolithic.backend.frontend.contract.api.TimeToMergeApi;
import fr.catlean.monolithic.backend.frontend.contract.api.model.GetCurveResponseContract;
import fr.catlean.monolithic.backend.frontend.contract.api.model.GetHistogramResponseContract;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static catlean.monolithic.backend.rest.api.adapter.mapper.PullRequestHistogramContractMapper.domainToContract;
import static catlean.monolithic.backend.rest.api.adapter.mapper.PullRequestHistogramContractMapper.errorToContract;
import static catlean.monolithic.backend.rest.api.adapter.mapper.TimeToMergeCurveMapper.curveToContract;
import static fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram.SIZE_LIMIT;
import static fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram.TIME_LIMIT;
import static org.springframework.http.ResponseEntity.internalServerError;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Tags(@Tag(name = "TimeToMerge"))
@AllArgsConstructor
public class TimeToMergeRestApiAdapter implements TimeToMergeApi {

    private final HistogramQuery histogramQuery;
    private final AuthenticationService authenticationService;
    private final CurveQuery curveQuery;

    @Override
    public ResponseEntity<GetCurveResponseContract> getTimeToMergeCurve(String teamName) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(curveToContract(curveQuery.computeTimeToMergeCurve(authenticatedUser.getOrganization(),
                    teamName)));
        } catch (CatleanException e) {
            return internalServerError().body(TimeToMergeCurveMapper.errorToContract(e));
        }
    }

    @Override
    public ResponseEntity<GetHistogramResponseContract> getTimeToMergeHistogramFromHistogramTable(String teamName) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return domainToContract(histogramQuery.readPullRequestHistogram(authenticatedUser.getOrganization(),
                    teamName, SIZE_LIMIT));
        } catch (CatleanException e) {
            return errorToContract(e);
        }
    }

    @Override
    public ResponseEntity<GetHistogramResponseContract> getTimeToMergeHistogram(String teamName) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return domainToContract(histogramQuery.computePullRequestHistogram(authenticatedUser.getOrganization(),
                    teamName, TIME_LIMIT));
        } catch (CatleanException e) {
            return errorToContract(e);
        }
    }
}
