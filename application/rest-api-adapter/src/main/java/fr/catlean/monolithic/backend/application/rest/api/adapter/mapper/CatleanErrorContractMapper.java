package fr.catlean.monolithic.backend.application.rest.api.adapter.mapper;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.frontend.contract.api.model.CatleanErrorContract;
import fr.catlean.monolithic.backend.frontend.contract.api.model.CatleanErrorsContract;

import java.util.List;

public interface CatleanErrorContractMapper {

    static CatleanErrorContract catleanExceptionToContract(CatleanException e) {
        final CatleanErrorContract catleanErrorContract = new CatleanErrorContract();
        catleanErrorContract.setCode(e.getCode());
        catleanErrorContract.setMessage(e.getMessage());
        return catleanErrorContract;
    }

    static CatleanErrorsContract catleanExceptionToContracts(CatleanException e) {
        final CatleanErrorsContract catleanErrorsContract = new CatleanErrorsContract();
        catleanErrorsContract.errors(List.of(catleanExceptionToContract(e)));
        return catleanErrorsContract;
    }
}
