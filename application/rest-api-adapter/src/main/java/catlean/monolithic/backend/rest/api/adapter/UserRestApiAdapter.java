package catlean.monolithic.backend.rest.api.adapter;

import catlean.monolithic.backend.rest.api.adapter.authentication.AuthenticationService;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.port.in.UserFacadeAdapter;
import fr.catlean.monolithic.backend.frontend.contract.api.UserApi;
import fr.catlean.monolithic.backend.frontend.contract.api.model.CurrentUserResponseContract;
import fr.catlean.monolithic.backend.frontend.contract.api.model.LinkOrganizationToCurrentUserRequestContract;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static catlean.monolithic.backend.rest.api.adapter.mapper.CatleanErrorResponseMapper.catleanExceptionToContract;
import static catlean.monolithic.backend.rest.api.adapter.mapper.UserResponseMapper.userToResponse;

@RestController
@Tags(@Tag(name = "User"))
@AllArgsConstructor
public class UserRestApiAdapter implements UserApi {

    private final AuthenticationService authenticationService;
    private final UserFacadeAdapter userFacadeAdapter;

    @Override
    public ResponseEntity<CurrentUserResponseContract> getCurrentUser() {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ResponseEntity.ok(userToResponse(authenticatedUser));
        } catch (CatleanException catleanException) {
            final CurrentUserResponseContract currentUserResponseContract = new CurrentUserResponseContract();
            currentUserResponseContract.setErrors(List.of(catleanExceptionToContract(catleanException)));
            return ResponseEntity.internalServerError().body(currentUserResponseContract);
        }
    }

    @Override
    public ResponseEntity<CurrentUserResponseContract> linkCurrentUserToOrganization(LinkOrganizationToCurrentUserRequestContract linkOrganizationToCurrentUserRequestContract) {
        try {
            User authenticatedUser = authenticationService.getAuthenticatedUser();
            authenticatedUser = userFacadeAdapter.updateUserWithOrganization(authenticatedUser,
                    linkOrganizationToCurrentUserRequestContract.getExternalId());
            return ResponseEntity.ok(userToResponse(authenticatedUser));
        } catch (CatleanException catleanException) {
            final CurrentUserResponseContract currentUserResponseContract = new CurrentUserResponseContract();
            currentUserResponseContract.setErrors(List.of(catleanExceptionToContract(catleanException)));
            return ResponseEntity.internalServerError().body(currentUserResponseContract);
        }
    }
}
