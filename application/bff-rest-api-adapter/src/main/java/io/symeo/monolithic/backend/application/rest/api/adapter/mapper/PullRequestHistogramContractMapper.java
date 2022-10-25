package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.bff.model.metric.DataCompareToLimit;
import io.symeo.monolithic.backend.domain.bff.model.metric.PullRequestHistogram;
import io.symeo.monolithic.backend.frontend.contract.api.model.SymeoErrorContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.GetHistogramResponseContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.HistogramDataResponseContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.HistogramResponseContract;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

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
        histogramResponseContract.setLimit(pullRequestHistogram.getLimit());
        final GetHistogramResponseContract getHistogramResponseContract = new GetHistogramResponseContract();
        getHistogramResponseContract.setHistogram(histogramResponseContract);
        return ok(getHistogramResponseContract);
    }

    static GetHistogramResponseContract errorToContract(SymeoException e) {
        final SymeoErrorContract symeoErrorContract = SymeoErrorContractMapper.exceptionToContract(e);
        final GetHistogramResponseContract getHistogramResponseContract = new GetHistogramResponseContract();
        getHistogramResponseContract.errors(List.of(symeoErrorContract));
        return getHistogramResponseContract;
    }


}
