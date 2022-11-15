package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.bff.contract.api.model.ApiKeyContract;
import io.symeo.monolithic.backend.bff.contract.api.model.CreateApiKeyResponseContract;
import io.symeo.monolithic.backend.bff.contract.api.model.GetApiKeysResponseContract;
import io.symeo.monolithic.backend.domain.bff.model.account.OrganizationApiKey;
import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.util.List;

public interface ApiKeysContractMapper {
    static CreateApiKeyResponseContract errorToCreateApiKeyResponseContract(final SymeoException symeoException) {
        final CreateApiKeyResponseContract createApiKeyResponseContract = new CreateApiKeyResponseContract();
        createApiKeyResponseContract.setErrors(List.of(SymeoErrorContractMapper.exceptionToContract(symeoException)));
        return createApiKeyResponseContract;
    }

    static GetApiKeysResponseContract errorToGetApiKeysResponseContract(final SymeoException symeoException) {
        final GetApiKeysResponseContract getApiKeysResponseContract = new GetApiKeysResponseContract();
        getApiKeysResponseContract.setErrors(List.of(SymeoErrorContractMapper.exceptionToContract(symeoException)));
        return getApiKeysResponseContract;
    }

    static CreateApiKeyResponseContract toCreateApiKeyResponseContract(final OrganizationApiKey apiKey) {
        final CreateApiKeyResponseContract createApiKeyResponseContract = new CreateApiKeyResponseContract();
        createApiKeyResponseContract.setApiKey(toCreateApiKeyContract(apiKey));
        return createApiKeyResponseContract;
    }

    static GetApiKeysResponseContract toGetApiKeysResponseContract(final List<OrganizationApiKey> apiKeys) {
        final GetApiKeysResponseContract getApiKeysResponseContract = new GetApiKeysResponseContract();
        getApiKeysResponseContract.setApiKeys(apiKeys.stream().map(ApiKeysContractMapper::toGetApiKeyContract).toList());
        return getApiKeysResponseContract;
    }

    static ApiKeyContract toCreateApiKeyContract(OrganizationApiKey apiKey) {
        final ApiKeyContract apiKeyContract = new ApiKeyContract();
        apiKeyContract.setId(apiKey.getId());
        apiKeyContract.setName(apiKey.getName());
        apiKeyContract.setValue(apiKey.getKey());

        return apiKeyContract;
    }

    static ApiKeyContract toGetApiKeyContract(OrganizationApiKey apiKey) {
        final ApiKeyContract apiKeyContract = new ApiKeyContract();
        apiKeyContract.setId(apiKey.getId());
        apiKeyContract.setName(apiKey.getName());
        apiKeyContract.setValue(apiKey.getHiddenKey());

        return apiKeyContract;
    }
}
