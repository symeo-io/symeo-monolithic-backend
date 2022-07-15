package catlean.monolithic.backend.rest.api.adapter.mapper;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.frontend.contract.api.model.CatleanErrorContract;

public interface CatleanErrorResponseMapper {

    static CatleanErrorContract catleanExceptionToContract(CatleanException e) {
        final CatleanErrorContract catleanErrorContract = new CatleanErrorContract();
        catleanErrorContract.setCode(e.getCode());
        catleanErrorContract.setMessage(e.getMessage());
        return catleanErrorContract;
    }
}
