package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.domain.model.account.Onboarding;
import io.symeo.monolithic.backend.frontend.contract.api.model.OnboardingContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.PostOnboardingResponseContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.UpdateOnboardingRequestContract;

public interface OnboardingContractMapper {

    static Onboarding getOnboarding(final Onboarding onboarding,final UpdateOnboardingRequestContract updateOnboardingRequestContract) {
        return onboarding.toBuilder()
                .hasConfiguredTeam(updateOnboardingRequestContract.getHasConfiguredTeam())
                .hasConnectedToVcs(updateOnboardingRequestContract.getHasConnectedToVcs())
                .build();
    }

    static PostOnboardingResponseContract getPostOnboardingResponseContract(final Onboarding onboarding) {
        final PostOnboardingResponseContract postOnboardingResponseContract = new PostOnboardingResponseContract();
        final OnboardingContract onboardingContract = new OnboardingContract();
        onboardingContract.setHasConfiguredTeam(onboarding.getHasConfiguredTeam());
        onboardingContract.setHasConnectedToVcs(onboarding.getHasConnectedToVcs());
        onboardingContract.setId(onboarding.getId());
        postOnboardingResponseContract.setOnboarding(onboardingContract);
        return postOnboardingResponseContract;
    }
}
