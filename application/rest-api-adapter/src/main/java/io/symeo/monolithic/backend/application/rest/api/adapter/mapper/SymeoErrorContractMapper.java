package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.frontend.contract.api.model.SymeoErrorContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.SymeoErrorsContract;

import java.util.List;

public interface SymeoErrorContractMapper {

    static SymeoErrorContract exceptionToContract(SymeoException e) {
        final SymeoErrorContract symeoErrorContract = new SymeoErrorContract();
        symeoErrorContract.setCode(e.getCode());
        symeoErrorContract.setMessage(e.getMessage());
        return symeoErrorContract;
    }

    static SymeoErrorsContract exceptionToContracts(SymeoException e) {
        final SymeoErrorsContract symeoErrorsContract = new SymeoErrorsContract();
        symeoErrorsContract.errors(List.of(exceptionToContract(e)));
        return symeoErrorsContract;
    }
}
