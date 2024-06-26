package io.symeo.monolithic.backend.application.rest.api.adapter.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import io.symeo.monolithic.backend.application.rest.api.adapter.authentication.AuthenticationService;
import io.symeo.monolithic.backend.application.rest.api.adapter.service.RepositoryRetryService;
import io.symeo.monolithic.backend.bff.contract.api.RepositoryApi;
import io.symeo.monolithic.backend.bff.contract.api.model.GetRepositoriesResponseContract;
import io.symeo.monolithic.backend.domain.bff.model.account.User;
import io.symeo.monolithic.backend.domain.bff.model.vcs.RepositoryView;
import io.symeo.monolithic.backend.domain.bff.service.vcs.RepositoryService;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.RepositoryContractMapper.domainToGetRepositoriesResponseContract;
import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.RepositoryContractMapper.domainToKo;
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
            List<RepositoryView> repositoriesForOrganization =
                    repositoryService.getRepositoriesForOrganization(authenticatedUser.getOrganization());
            if (repositoriesForOrganization.isEmpty()) {
                repositoryRetryService.checkAndRetryOnCollectionJobs(authenticatedUser.getOrganization());
                repositoriesForOrganization =
                        repositoryService.getRepositoriesForOrganization(authenticatedUser.getOrganization());
            }
            return ok(domainToGetRepositoriesResponseContract(repositoriesForOrganization));
        } catch (SymeoException e) {
            return mapSymeoExceptionToContract(() -> domainToKo(e), e);
        }

    }


}
