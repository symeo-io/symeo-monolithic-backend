package io.symeo.monolithic.backend.job.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import io.symeo.monolithic.backend.data.processing.contract.api.TestingApi;
import io.symeo.monolithic.backend.data.processing.contract.api.model.CollectTestingDataRequestContract;
import io.symeo.monolithic.backend.data.processing.contract.api.model.DataProcessingSymeoErrorsContract;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.testing.CommitTestingData;
import io.symeo.monolithic.backend.job.domain.port.in.CommitTestingDataFacadeAdapter;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static io.symeo.monolithic.backend.job.rest.api.adapter.mapper.SymeoErrorContractMapper.dataProcessingExceptionToContracts;
import static io.symeo.monolithic.backend.job.rest.api.adapter.mapper.SymeoErrorContractMapper.mapSymeoExceptionToContract;
import static io.symeo.monolithic.backend.job.rest.api.adapter.mapper.TestingContractMapper.contractToDomain;

@AllArgsConstructor
@RestController
@Tags(@Tag(name = "Testing"))
public class TestingRestApiAdapter implements TestingApi {
    private final CommitTestingDataFacadeAdapter commitTestingDataFacadeAdapter;

    @Override
    public ResponseEntity<DataProcessingSymeoErrorsContract> collectTestingData(String X_API_KEY, CollectTestingDataRequestContract collectTestingDataRequestContract) {
        final CommitTestingData commitTestingData;
        try {
            commitTestingData = contractToDomain(collectTestingDataRequestContract);
            commitTestingDataFacadeAdapter.save(commitTestingData);
            return ResponseEntity.ok().build();
        } catch (SymeoException e) {
            return mapSymeoExceptionToContract(() -> dataProcessingExceptionToContracts(e), e);
        }
    }
}
