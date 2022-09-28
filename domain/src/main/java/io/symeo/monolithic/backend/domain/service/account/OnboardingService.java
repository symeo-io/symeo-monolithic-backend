package io.symeo.monolithic.backend.domain.service.account;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Onboarding;
import io.symeo.monolithic.backend.domain.port.in.OnboardingFacadeAdapter;
import io.symeo.monolithic.backend.domain.port.out.OnboardingStorage;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OnboardingService implements OnboardingFacadeAdapter {

    private final OnboardingStorage onboardingStorage;

    @Override
    public Onboarding updateOnboarding(Onboarding onboarding) throws SymeoException {
        return onboardingStorage.updateOnboarding(onboarding);
    }
}
