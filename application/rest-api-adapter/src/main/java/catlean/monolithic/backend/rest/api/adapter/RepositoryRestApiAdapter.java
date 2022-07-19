package catlean.monolithic.backend.rest.api.adapter;

import catlean.monolithic.backend.rest.api.adapter.authentication.AuthenticationService;
import catlean.monolithic.backend.rest.api.adapter.mapper.RepositoryResponseMapper;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.service.RepositoryService;
import fr.catlean.monolithic.backend.frontend.contract.api.RepositoryApi;
import fr.catlean.monolithic.backend.frontend.contract.api.model.GetRepositoriesResponseContract;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static catlean.monolithic.backend.rest.api.adapter.mapper.RepositoryResponseMapper.domainToGetRepositoriesResponseContract;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@Tags(@Tag(name = "Repository"))
public class RepositoryRestApiAdapter implements RepositoryApi {

    private final AuthenticationService authenticationService;
    private final RepositoryService repositoryService;

    @Override
    public ResponseEntity<GetRepositoriesResponseContract> apiV1RepositoryGet() {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(domainToGetRepositoriesResponseContract(
                    repositoryService.getRepositoriesForOrganization(authenticatedUser.getOrganization()))
            );
        } catch (CatleanException e) {
            return ResponseEntity.internalServerError().body(RepositoryResponseMapper.domainToKo(e));
        }

    }
}
