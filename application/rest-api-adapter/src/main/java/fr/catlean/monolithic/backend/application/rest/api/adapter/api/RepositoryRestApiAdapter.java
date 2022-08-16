package fr.catlean.monolithic.backend.application.rest.api.adapter.api;

import fr.catlean.monolithic.backend.application.rest.api.adapter.authentication.AuthenticationService;
import fr.catlean.monolithic.backend.application.rest.api.adapter.mapper.RepositoryContractMapper;
import fr.catlean.monolithic.backend.application.rest.api.adapter.service.RepositoryRetryService;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.service.platform.vcs.RepositoryService;
import fr.catlean.monolithic.backend.frontend.contract.api.RepositoryApi;
import fr.catlean.monolithic.backend.frontend.contract.api.model.GetRepositoriesResponseContract;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static fr.catlean.monolithic.backend.application.rest.api.adapter.mapper.RepositoryContractMapper.domainToGetRepositoriesResponseContract;
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
        } catch (CatleanException e) {
            return ResponseEntity.internalServerError().body(RepositoryContractMapper.domainToKo(e));
        }

    }


}
