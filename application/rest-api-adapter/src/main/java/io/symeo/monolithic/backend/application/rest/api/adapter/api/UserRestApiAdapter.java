package io.symeo.monolithic.backend.application.rest.api.adapter.api;

import io.symeo.monolithic.backend.application.rest.api.adapter.authentication.AuthenticationService;
import io.symeo.monolithic.backend.application.rest.api.adapter.mapper.OrganizationSettingsContractMapper;
import io.symeo.monolithic.backend.application.rest.api.adapter.mapper.SymeoErrorContractMapper;
import io.symeo.monolithic.backend.application.rest.api.adapter.mapper.UserContractMapper;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Onboarding;
import io.symeo.monolithic.backend.domain.model.account.User;
import io.symeo.monolithic.backend.domain.port.in.OnboardingFacadeAdapter;
import io.symeo.monolithic.backend.domain.port.in.UserFacadeAdapter;
import io.symeo.monolithic.backend.frontend.contract.api.UserApi;
import io.symeo.monolithic.backend.frontend.contract.api.model.CurrentUserResponseContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.LinkOrganizationToCurrentUserRequestContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.PostOnboardingResponseContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.UpdateOnboardingRequestContract;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import io.symeo.monolithic.backend.application.rest.api.adapter.mapper.OnboardingContractMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.SymeoErrorContractMapper.mapSymeoExceptionToContract;
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
            return ok(UserContractMapper.currentUserToResponse(authenticatedUser));
        } catch (SymeoException symeoException) {
            return mapSymeoExceptionToContract(() -> UserContractMapper.currentUsersToError(symeoException), symeoException);
        }
    }

    @Override
    public ResponseEntity<CurrentUserResponseContract> linkCurrentUserToOrganization(LinkOrganizationToCurrentUserRequestContract linkOrganizationToCurrentUserRequestContract) {
        try {
            User authenticatedUser = authenticationService.getAuthenticatedUser();
            authenticatedUser = userFacadeAdapter.updateUserWithOrganization(authenticatedUser,
                    linkOrganizationToCurrentUserRequestContract.getExternalId());
            return ok(UserContractMapper.currentUserToResponse(authenticatedUser));
        } catch (SymeoException symeoException) {
            return mapSymeoExceptionToContract(() -> UserContractMapper.currentUsersToError(symeoException), symeoException);
        }
    }

    @Override
    public ResponseEntity<PostOnboardingResponseContract> updateOnboarding(UpdateOnboardingRequestContract updateOnboardingRequestContract) {
        try {
            User authenticatedUser = authenticationService.getAuthenticatedUser();
            Onboarding onboarding = OnboardingContractMapper.getOnboarding(authenticatedUser.getOnboarding(), updateOnboardingRequestContract);
            onboarding = onboardingFacadeAdapter.updateOnboarding(onboarding);
            return ok(OnboardingContractMapper.getPostOnboardingResponseContract(onboarding));
        } catch (SymeoException symeoException) {
            return mapSymeoExceptionToContract(() -> OnboardingContractMapper.exceptionToContract(symeoException), symeoException);
        }
    }


}
