package io.symeo.monolithic.backend.domain.bff.service.organization;

import io.symeo.monolithic.backend.domain.bff.model.account.Onboarding;
import io.symeo.monolithic.backend.domain.bff.port.in.OnboardingFacadeAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.OnboardingStorage;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OnboardingService implements OnboardingFacadeAdapter {

    private final OnboardingStorage onboardingStorage;

    @Override
    public Onboarding updateOnboarding(Onboarding onboarding) throws SymeoException {
        return onboardingStorage.updateOnboarding(onboarding);
    }
}
