package catlean.monolithic.backend.rest.api.adapter.api;


import catlean.monolithic.backend.rest.api.adapter.authentication.AuthenticationService;
import catlean.monolithic.backend.rest.api.adapter.mapper.CatleanErrorContractMapper;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.port.in.UserFacadeAdapter;
import fr.catlean.monolithic.backend.frontend.contract.api.OrganizationApi;
import fr.catlean.monolithic.backend.frontend.contract.api.model.DeleteUserResponseContract;
import fr.catlean.monolithic.backend.frontend.contract.api.model.UserRequestContract;
import fr.catlean.monolithic.backend.frontend.contract.api.model.UsersResponseContract;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static catlean.monolithic.backend.rest.api.adapter.mapper.UserContractMapper.*;
import static org.springframework.http.ResponseEntity.internalServerError;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Tags(@Tag(name = "Organization"))
@AllArgsConstructor
public class OrganizationRestApiAdapter implements OrganizationApi {

    private final AuthenticationService authenticationService;
    private final UserFacadeAdapter userFacadeAdapter;

    @Override
    public ResponseEntity<UsersResponseContract> createUsersToOrganization(List<UserRequestContract> userRequestContract) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(usersToResponse(userFacadeAdapter.inviteUsersForOrganization(authenticatedUser.getOrganization(),
                    authenticatedUser, contractToUsers(userRequestContract))));
        } catch (CatleanException e) {
            return internalServerError().body(usersToError(e));
        }
    }

    @Override
    public ResponseEntity<UsersResponseContract> getUsersFromOrganization() {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(usersToResponse(userFacadeAdapter.getAllUsersForOrganization(authenticatedUser.getOrganization())));
        } catch (CatleanException e) {
            return internalServerError().body(usersToError(e));
        }
    }

    @Override
    public ResponseEntity<DeleteUserResponseContract> removeUserFromOrganization(UUID id) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            userFacadeAdapter.removeUserFromOrganization(id, authenticatedUser.getOrganization());
            return ResponseEntity.ok(new DeleteUserResponseContract());
        } catch (CatleanException e) {
            final DeleteUserResponseContract deletedUserResponseContract = new DeleteUserResponseContract();
            deletedUserResponseContract.setErrors(List.of(CatleanErrorContractMapper.catleanExceptionToContract(e)));
            return ResponseEntity.internalServerError().body(deletedUserResponseContract);
        }
    }
}
