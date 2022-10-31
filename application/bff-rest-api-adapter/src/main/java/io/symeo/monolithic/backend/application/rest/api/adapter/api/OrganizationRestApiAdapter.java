package io.symeo.monolithic.backend.application.rest.api.adapter.api;


import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import io.symeo.monolithic.backend.application.rest.api.adapter.authentication.AuthenticationService;
import io.symeo.monolithic.backend.application.rest.api.adapter.mapper.SymeoErrorContractMapper;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.bff.model.account.User;
import io.symeo.monolithic.backend.domain.bff.port.in.OrganizationSettingsFacade;
import io.symeo.monolithic.backend.domain.bff.port.in.UserFacadeAdapter;
import io.symeo.monolithic.backend.bff.contract.api.OrganizationApi;
import io.symeo.monolithic.backend.bff.contract.api.model.*;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.OrganizationSettingsContractMapper.*;
import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.UserContractMapper.*;
import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.SymeoErrorContractMapper.mapSymeoExceptionToContract;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Tags(@Tag(name = "Organization"))
@AllArgsConstructor
public class OrganizationRestApiAdapter implements OrganizationApi {

    private final AuthenticationService authenticationService;
    private final UserFacadeAdapter userFacadeAdapter;
    private final OrganizationSettingsFacade organizationSettingsFacade;

    @Override
    public ResponseEntity<UsersResponseContract> createUsersToOrganization(List<UserRequestContract> userRequestContract) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(usersToResponse(userFacadeAdapter.inviteUsersForOrganization(authenticatedUser.getOrganization(),
                    authenticatedUser, contractToUsers(userRequestContract))));
        } catch (SymeoException e) {
            return mapSymeoExceptionToContract(() -> usersToError(e), e);
        }
    }

    @Override
    public ResponseEntity<UsersResponseContract> getUsersFromOrganization() {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(usersToResponse(userFacadeAdapter.getAllUsersForOrganization(authenticatedUser.getOrganization())));
        } catch (SymeoException e) {
            return mapSymeoExceptionToContract(() -> usersToError(e), e);
        }
    }

    @Override
    public ResponseEntity<DeleteUserResponseContract> removeUserFromOrganization(UUID id) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            userFacadeAdapter.removeUserFromOrganization(id, authenticatedUser.getOrganization());
            return ok(new DeleteUserResponseContract());
        } catch (SymeoException e) {
            return mapSymeoExceptionToContract(() -> exceptionToContract(e), e);
        }
    }

    @Override
    public ResponseEntity<OrganizationSettingsResponseContract> getOrganizationSettings() {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(domainToContract(organizationSettingsFacade.getOrganizationSettingsForOrganization(authenticatedUser.getOrganization())));
        } catch (SymeoException e) {
            return mapSymeoExceptionToContract(() -> errorToContract(e), e);
        }
    }

    @Override
    public ResponseEntity<SymeoErrorsContract> updateOrganizationSettings(OrganizationSettingsContract organizationSettingsContract) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            organizationSettingsFacade.updateOrganizationSettings(contractToDomain(organizationSettingsContract, authenticatedUser.getOrganization().getId()));
            return ok().build();
        } catch (SymeoException e) {
            return mapSymeoExceptionToContract(() -> SymeoErrorContractMapper.exceptionToContracts(e), e);
        }

    }
}
