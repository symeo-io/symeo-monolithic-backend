package catlean.monolithic.backend.rest.api.adapter.mapper;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.insight.DataCompareToLimit;
import fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram;
import fr.catlean.monolithic.backend.frontend.contract.api.model.CatleanErrorContract;
import fr.catlean.monolithic.backend.frontend.contract.api.model.GetHistogramResponseContract;
import fr.catlean.monolithic.backend.frontend.contract.api.model.HistogramDataResponseContract;
import fr.catlean.monolithic.backend.frontend.contract.api.model.HistogramResponseContract;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static catlean.monolithic.backend.rest.api.adapter.mapper.CatleanErrorContractMapper.catleanExceptionToContract;
import static org.springframework.http.ResponseEntity.internalServerError;
import static org.springframework.http.ResponseEntity.ok;

public interface PullRequestHistogramContractMapper {


    static ResponseEntity<GetHistogramResponseContract> domainToContract(final PullRequestHistogram pullRequestHistogram) {
        final HistogramResponseContract histogramResponseContract = new HistogramResponseContract();
        final List<HistogramDataResponseContract> histogramDataResponseContracts = new ArrayList<>();
        for (DataCompareToLimit dataCompareToLimit : pullRequestHistogram.getDataByWeek()) {
            final HistogramDataResponseContract histogramDataResponseContract = new HistogramDataResponseContract();
            histogramDataResponseContract.setDataAboveLimit(dataCompareToLimit.getNumberAboveLimit());
            histogramDataResponseContract.setDataBelowLimit(dataCompareToLimit.getNumberBelowLimit());
            histogramDataResponseContract.setStartDateRange(dataCompareToLimit.getDateAsString());
            histogramDataResponseContracts.add(histogramDataResponseContract);
        }
        histogramResponseContract.setData(
                histogramDataResponseContracts
        );
        final GetHistogramResponseContract getHistogramResponseContract = new GetHistogramResponseContract();
        getHistogramResponseContract.setHistogram(histogramResponseContract);
        return ok(getHistogramResponseContract);
    }

    static ResponseEntity<GetHistogramResponseContract> errorToContract(CatleanException e) {
        final CatleanErrorContract catleanErrorContract = catleanExceptionToContract(e);
        final GetHistogramResponseContract getHistogramResponseContract = new GetHistogramResponseContract();
        getHistogramResponseContract.errors(List.of(catleanErrorContract));
        return internalServerError().body(getHistogramResponseContract);
    }


}
