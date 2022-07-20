package catlean.monolithic.backend.rest.api.adapter.mapper;

import fr.catlean.monolithic.backend.domain.model.Onboarding;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.frontend.contract.api.model.CurrentUserResponseContract;
import fr.catlean.monolithic.backend.frontend.contract.api.model.OnboardingContract;
import fr.catlean.monolithic.backend.frontend.contract.api.model.OrganizationContract;
import fr.catlean.monolithic.backend.frontend.contract.api.model.UserContract;

import static java.util.Objects.nonNull;

public interface UserResponseMapper {

    static CurrentUserResponseContract userToResponse(final User user) {
        final CurrentUserResponseContract currentUserResponseContract = new CurrentUserResponseContract();
        currentUserResponseContract.setUser(userToResponseContract(user));
        return currentUserResponseContract;
    }

    private static UserContract userToResponseContract(final User user) {
        final UserContract userContract = new UserContract();
        userContract.setEmail(user.getMail());
        userContract.setId(user.getId());
        if (nonNull(user.getOrganization())) {
            userContract.setOrganization(organizationToContract(user.getOrganization()));
        }
        if (nonNull(user.getOnboarding())) {
            userContract.setOnboarding(onboardingToContract(user.getOnboarding()));
        }
        return userContract;
    }

    private static OrganizationContract organizationToContract(final Organization organization) {
        final OrganizationContract organizationContract = new OrganizationContract();
        organizationContract.setId(organization.getId());
        organizationContract.setName(organization.getName());
        return organizationContract;
    }

    private static OnboardingContract onboardingToContract(final Onboarding onboarding) {
        final OnboardingContract onboardingContract = new OnboardingContract();
        onboardingContract.setHasConnectedToVcs(onboarding.getHasConnectedToVcs());
        onboardingContract.setHasConfiguredTeam(onboarding.getHasConfiguredTeam());
        return onboardingContract;
    }
}
