package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.insight.LeadTimeMetrics;
import io.symeo.monolithic.backend.frontend.contract.api.model.LeadTimeResponseContract;

import java.util.List;

public interface LeadTimeContractMapper {


    static LeadTimeResponseContract errorToContract(final SymeoException symeoException) {
        final LeadTimeResponseContract leadTimeResponseContract = new LeadTimeResponseContract();
        leadTimeResponseContract.setErrors(List.of(SymeoErrorContractMapper.exceptionToContract(symeoException)));
        return leadTimeResponseContract;
    }

    static LeadTimeResponseContract toContract(final LeadTimeMetrics leadTimeMetrics) {


    }
}
