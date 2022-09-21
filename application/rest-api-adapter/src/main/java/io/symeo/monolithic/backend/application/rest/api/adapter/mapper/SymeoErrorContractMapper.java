package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.data.processing.contract.api.model.DataProcessingSymeoErrorContract;
import io.symeo.monolithic.backend.data.processing.contract.api.model.DataProcessingSymeoErrorsContract;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.frontend.contract.api.model.SymeoErrorContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.SymeoErrorsContract;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.function.Supplier;

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

    static DataProcessingSymeoErrorContract dataProcessingExceptionToContract(SymeoException e) {
        final DataProcessingSymeoErrorContract dataProcessingSymeoErrorContract = new DataProcessingSymeoErrorContract();
        dataProcessingSymeoErrorContract.setCode(e.getCode());
        dataProcessingSymeoErrorContract.setMessage(e.getMessage());
        return dataProcessingSymeoErrorContract;
    }

    static DataProcessingSymeoErrorsContract dataProcessingExceptionToContracts(SymeoException e) {
        final DataProcessingSymeoErrorsContract dataProcessingSymeoErrorsContract = new DataProcessingSymeoErrorsContract();
        dataProcessingSymeoErrorsContract.errors(List.of(dataProcessingExceptionToContract(e)));
        return dataProcessingSymeoErrorsContract;
    }

    static <T> ResponseEntity<T> mapSymeoExceptionToContract(final Supplier<T> tSupplier, SymeoException symeoException) {
        if (SymeoException.isFunctional(symeoException)) {
            return ResponseEntity.badRequest().body(tSupplier.get());
        } else {
            return ResponseEntity.internalServerError().body(tSupplier.get());
        }
    }
}
