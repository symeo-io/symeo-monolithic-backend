package io.symeo.monolithic.backend.application.rest.api.adapter.api;


import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import io.symeo.monolithic.backend.application.rest.api.adapter.authentication.AuthenticationService;
import io.symeo.monolithic.backend.application.rest.api.adapter.mapper.SymeoErrorContractMapper;
import io.symeo.monolithic.backend.application.rest.api.adapter.mapper.UserContractMapper;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import io.symeo.monolithic.backend.domain.model.account.User;
import io.symeo.monolithic.backend.domain.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.port.in.OrganizationSettingsFacade;
import io.symeo.monolithic.backend.domain.port.in.UserFacadeAdapter;
import io.symeo.monolithic.backend.frontend.contract.api.OrganizationApi;
import io.symeo.monolithic.backend.frontend.contract.api.model.*;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.OrganizationSettingsContractMapper.*;
import static org.springframework.http.ResponseEntity.internalServerError;
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
            return ok(UserContractMapper.usersToResponse(userFacadeAdapter.inviteUsersForOrganization(authenticatedUser.getOrganization(),
                    authenticatedUser, UserContractMapper.contractToUsers(userRequestContract))));
        } catch (SymeoException e) {
            return internalServerError().body(UserContractMapper.usersToError(e));
        }
    }

    @Override
    public ResponseEntity<UsersResponseContract> getUsersFromOrganization() {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(UserContractMapper.usersToResponse(userFacadeAdapter.getAllUsersForOrganization(authenticatedUser.getOrganization())));
        } catch (SymeoException e) {
            return internalServerError().body(UserContractMapper.usersToError(e));
        }
    }

    @Override
    public ResponseEntity<DeleteUserResponseContract> removeUserFromOrganization(UUID id) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            userFacadeAdapter.removeUserFromOrganization(id, authenticatedUser.getOrganization());
            return ok(new DeleteUserResponseContract());
        } catch (SymeoException e) {
            final DeleteUserResponseContract deletedUserResponseContract = new DeleteUserResponseContract();
            deletedUserResponseContract.setErrors(List.of(SymeoErrorContractMapper.exceptionToContract(e)));
            return internalServerError().body(deletedUserResponseContract);
        }
    }

    @Override
    public ResponseEntity<OrganizationSettingsResponseContract> getOrganizationSettings() {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(domainToContract(organizationSettingsFacade.getOrganizationSettingsForOrganization(authenticatedUser.getOrganization())));
        } catch (SymeoException e) {
            return internalServerError().body(errorToContract(e));
        }
    }

    @Override
    public ResponseEntity<SymeoErrorsContract> updateOrganizationSettings(OrganizationSettingsContract organizationSettingsContract) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            final UUID organizationId = authenticatedUser.getOrganization().getId();
            final Optional<OrganizationSettings> organizationSettings = organizationSettingsFacade.getOrganizationSettingsForId(organizationSettingsContract.getId());

            if (organizationSettings.isPresent() && organizationSettings.get().getOrganizationId().equals(organizationId)) {
                final OrganizationSettings updatedOrganizationSettings = contractToDomain(organizationSettingsContract, organizationId);
                organizationSettingsFacade.updateOrganizationSettings(updatedOrganizationSettings);
                return ok().build();
            } else {
                return internalServerError().body(SymeoErrorContractMapper.exceptionToContracts(SymeoException.builder()
                        .code(SymeoExceptionCode.ORGANIZATION_SETTINGS_NOT_FOUND)
                        .message(String.format("OrganizationSettings not found for organizationId %s or user not authorized to change organizationSettings",
                                organizationId))
                        .build()));
            }
        } catch (SymeoException e) {
            return internalServerError().body(SymeoErrorContractMapper.exceptionToContracts(e));
        }

    }
}
