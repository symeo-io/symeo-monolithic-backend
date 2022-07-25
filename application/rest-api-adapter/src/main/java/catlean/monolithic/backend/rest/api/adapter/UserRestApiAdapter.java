package catlean.monolithic.backend.rest.api.adapter;

import catlean.monolithic.backend.rest.api.adapter.authentication.AuthenticationService;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Onboarding;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.port.in.OnboardingFacadeAdapter;
import fr.catlean.monolithic.backend.domain.port.in.UserFacadeAdapter;
import fr.catlean.monolithic.backend.frontend.contract.api.UserApi;
import fr.catlean.monolithic.backend.frontend.contract.api.model.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static catlean.monolithic.backend.rest.api.adapter.mapper.CatleanErrorContractMapper.catleanExceptionToContract;
import static catlean.monolithic.backend.rest.api.adapter.mapper.OnboardingContractMapper.getOnboarding;
import static catlean.monolithic.backend.rest.api.adapter.mapper.OnboardingContractMapper.getPostOnboardingResponseContract;
import static catlean.monolithic.backend.rest.api.adapter.mapper.UserContractMapper.*;
import static org.springframework.http.ResponseEntity.internalServerError;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Tags(@Tag(name = "User"))
@AllArgsConstructor
public class UserRestApiAdapter implements UserApi {

    private final AuthenticationService authenticationService;
    private final UserFacadeAdapter userFacadeAdapter;
    private final OnboardingFacadeAdapter onboardingFacadeAdapter;

    @Override
    public ResponseEntity<CurrentUserResponseContract> getCurrentUser() {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(currentUserToResponse(authenticatedUser));
        } catch (CatleanException catleanException) {
            final CurrentUserResponseContract currentUserResponseContract = new CurrentUserResponseContract();
            currentUserResponseContract.setErrors(List.of(catleanExceptionToContract(catleanException)));
            return internalServerError().body(currentUserResponseContract);
        }
    }

    @Override
    public ResponseEntity<CurrentUserResponseContract> linkCurrentUserToOrganization(LinkOrganizationToCurrentUserRequestContract linkOrganizationToCurrentUserRequestContract) {
        try {
            User authenticatedUser = authenticationService.getAuthenticatedUser();
            authenticatedUser = userFacadeAdapter.updateUserWithOrganization(authenticatedUser,
                    linkOrganizationToCurrentUserRequestContract.getExternalId());
            return ok(currentUserToResponse(authenticatedUser));
        } catch (CatleanException catleanException) {
            final CurrentUserResponseContract currentUserResponseContract = new CurrentUserResponseContract();
            currentUserResponseContract.setErrors(List.of(catleanExceptionToContract(catleanException)));
            return internalServerError().body(currentUserResponseContract);
        }
    }

    @Override
    public ResponseEntity<PostOnboardingResponseContract> updateOnboarding(UpdateOnboardingRequestContract updateOnboardingRequestContract) {
        try {
            User authenticatedUser = authenticationService.getAuthenticatedUser();
            Onboarding onboarding = getOnboarding(authenticatedUser.getOnboarding(), updateOnboardingRequestContract);
            onboarding = onboardingFacadeAdapter.updateOnboarding(onboarding);
            return ok(getPostOnboardingResponseContract(onboarding));
        } catch (CatleanException catleanException) {
            final PostOnboardingResponseContract postOnboardingResponseContract = new PostOnboardingResponseContract();
            return internalServerError().body(postOnboardingResponseContract);
        }
    }


    @Override
    public ResponseEntity<UsersResponseContract> getUsers() {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(usersToResponse(userFacadeAdapter.getAllUsersForOrganization(authenticatedUser.getOrganization())));
        } catch (CatleanException e) {
            return internalServerError().body(usersToError(e));
        }
    }

    @Override
    public ResponseEntity<UsersResponseContract> postUsers(List<UserRequestContract> userRequestContract) {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            return ok(usersToResponse(userFacadeAdapter.createUsersForOrganization(authenticatedUser.getOrganization(), contractToUsers(userRequestContract))));
        } catch (CatleanException e) {
            return internalServerError().body(usersToError(e));
        }
    }
}
