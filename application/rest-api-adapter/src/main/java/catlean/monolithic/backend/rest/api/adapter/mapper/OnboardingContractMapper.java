package catlean.monolithic.backend.rest.api.adapter.mapper;

import fr.catlean.monolithic.backend.domain.model.account.Onboarding;
import fr.catlean.monolithic.backend.frontend.contract.api.model.OnboardingContract;
import fr.catlean.monolithic.backend.frontend.contract.api.model.PostOnboardingResponseContract;

public interface OnboardingContractMapper {

    static Onboarding getOnboarding(OnboardingContract onboardingContract) {
        return Onboarding.builder()
                .id(onboardingContract.getId())
                .hasConfiguredTeam(onboardingContract.getHasConfiguredTeam())
                .hasConnectedToVcs(onboardingContract.getHasConnectedToVcs())
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
