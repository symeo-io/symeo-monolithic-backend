package io.symeo.monolithic.backend.application.rest.api.adapter.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import io.symeo.monolithic.backend.application.rest.api.adapter.authentication.AuthenticationService;
import io.symeo.monolithic.backend.application.rest.api.adapter.mapper.SymeoErrorContractMapper;
import io.symeo.monolithic.backend.bff.contract.api.ApiKeysApi;
import io.symeo.monolithic.backend.bff.contract.api.model.CreateApiKeyRequestContract;
import io.symeo.monolithic.backend.bff.contract.api.model.CreateApiKeyResponseContract;
import io.symeo.monolithic.backend.bff.contract.api.model.GetApiKeysResponseContract;
import io.symeo.monolithic.backend.bff.contract.api.model.SymeoErrorsContract;
import io.symeo.monolithic.backend.domain.bff.model.account.User;
import io.symeo.monolithic.backend.domain.bff.port.in.OrganizationApiKeyFacadeAdapter;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.ApiKeysContractMapper.*;
import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.SymeoErrorContractMapper.mapSymeoExceptionToContract;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Tags(@Tag(name = "ApiKeys"))
@AllArgsConstructor
public class ApiKeysRestApiAdapter implements ApiKeysApi {

    private final AuthenticationService authenticationService;

    private final OrganizationApiKeyFacadeAdapter organizationApiKeyFacadeAdapter;

    @Override
    public ResponseEntity<CreateApiKeyResponseContract> createNewApiKeyForOrganization(CreateApiKeyRequestContract createApiKeyRequestContract) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(toCreateApiKeyResponseContract(organizationApiKeyFacadeAdapter.createApiKeyForOrganizationIdAndName(authenticatedUser.getOrganization().getId(), createApiKeyRequestContract.getName())));
        } catch (SymeoException symeoException) {
            return mapSymeoExceptionToContract(() -> errorToCreateApiKeyResponseContract(symeoException), symeoException);
        }

    }

    @Override
    public ResponseEntity<GetApiKeysResponseContract> getApiKeysForOrganization() {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(toGetApiKeysResponseContract(organizationApiKeyFacadeAdapter.getOrganizationApiKeys(authenticatedUser.getOrganization().getId())));
        } catch (SymeoException symeoException) {
            return mapSymeoExceptionToContract(() -> errorToGetApiKeysResponseContract(symeoException), symeoException);
        }
    }

    @Override
    public ResponseEntity<SymeoErrorsContract> deleteApiKeyForOrganization(UUID apiKeyId) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            organizationApiKeyFacadeAdapter.deleteApiKeyForOrganizationId(apiKeyId, authenticatedUser.getOrganization().getId());
            return ok().build();
        } catch (SymeoException symeoException) {
            return mapSymeoExceptionToContract(() -> SymeoErrorContractMapper.exceptionToContracts(symeoException), symeoException);
        }
    }
}
