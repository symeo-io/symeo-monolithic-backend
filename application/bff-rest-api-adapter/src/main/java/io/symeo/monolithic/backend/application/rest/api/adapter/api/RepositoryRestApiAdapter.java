package io.symeo.monolithic.backend.application.rest.api.adapter.api;

import io.symeo.monolithic.backend.application.rest.api.adapter.authentication.AuthenticationService;
import io.symeo.monolithic.backend.application.rest.api.adapter.service.RepositoryRetryService;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.bff.model.account.User;
import io.symeo.monolithic.backend.domain.bff.service.vcs.RepositoryService;
import io.symeo.monolithic.backend.frontend.contract.api.RepositoryApi;
import io.symeo.monolithic.backend.frontend.contract.api.model.GetRepositoriesResponseContract;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.RepositoryContractMapper.*;
import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.RepositoryContractMapper.domainToGetRepositoriesResponseContract;
import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.SymeoErrorContractMapper.mapSymeoExceptionToContract;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@Tags(@Tag(name = "Repository"))
public class RepositoryRestApiAdapter implements RepositoryApi {

    private final AuthenticationService authenticationService;
    private final RepositoryService repositoryService;
    private final RepositoryRetryService repositoryRetryService;

    @Override
    public ResponseEntity<GetRepositoriesResponseContract> apiV1RepositoriesGet() {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            repositoryRetryService.checkAndRetryOnCollectionJobs(authenticatedUser.getOrganization());
            return ok(domainToGetRepositoriesResponseContract(
                    repositoryService.getRepositoriesForOrganization(authenticatedUser.getOrganization()))
            );
        } catch (SymeoException e) {
            return mapSymeoExceptionToContract(() -> domainToKo(e), e);
        }

    }


}
