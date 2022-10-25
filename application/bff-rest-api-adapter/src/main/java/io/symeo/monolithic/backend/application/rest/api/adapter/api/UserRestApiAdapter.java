package io.symeo.monolithic.backend.application.rest.api.adapter.api;

import io.symeo.monolithic.backend.application.rest.api.adapter.authentication.AuthenticationService;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.bff.model.account.Onboarding;
import io.symeo.monolithic.backend.domain.bff.model.account.User;
import io.symeo.monolithic.backend.domain.bff.port.in.OnboardingFacadeAdapter;
import io.symeo.monolithic.backend.domain.bff.port.in.UserFacadeAdapter;
import io.symeo.monolithic.backend.frontend.contract.api.UserApi;
import io.symeo.monolithic.backend.frontend.contract.api.model.CurrentUserResponseContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.LinkOrganizationToCurrentUserRequestContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.PostOnboardingResponseContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.UpdateOnboardingRequestContract;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.OnboardingContractMapper.*;
import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.SymeoErrorContractMapper.mapSymeoExceptionToContract;
import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.UserContractMapper.currentUsersToError;
import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.UserContractMapper.currentUserToResponse;
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
        } catch (SymeoException symeoException) {
            return mapSymeoExceptionToContract(() -> currentUsersToError(symeoException), symeoException);
        }
    }

    @Override
    public ResponseEntity<CurrentUserResponseContract> linkCurrentUserToOrganization(LinkOrganizationToCurrentUserRequestContract linkOrganizationToCurrentUserRequestContract) {
        try {
            User authenticatedUser = authenticationService.getAuthenticatedUser();
            authenticatedUser = userFacadeAdapter.updateUserWithOrganization(authenticatedUser,
                    linkOrganizationToCurrentUserRequestContract.getExternalId());
            return ok(currentUserToResponse(authenticatedUser));
        } catch (SymeoException symeoException) {
            return mapSymeoExceptionToContract(() -> currentUsersToError(symeoException), symeoException);
        }
    }

    @Override
    public ResponseEntity<PostOnboardingResponseContract> updateOnboarding(UpdateOnboardingRequestContract updateOnboardingRequestContract) {
        try {
            User authenticatedUser = authenticationService.getAuthenticatedUser();
            Onboarding onboarding = getOnboarding(authenticatedUser.getOnboarding(), updateOnboardingRequestContract);
            onboarding = onboardingFacadeAdapter.updateOnboarding(onboarding);
            return ok(getPostOnboardingResponseContract(onboarding));
        } catch (SymeoException symeoException) {
            return mapSymeoExceptionToContract(() -> exceptionToContract(symeoException), symeoException);
        }
    }


}
