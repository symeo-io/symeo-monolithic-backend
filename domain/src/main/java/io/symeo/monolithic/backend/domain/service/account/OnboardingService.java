package io.symeo.monolithic.backend.domain.service.account;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Onboarding;
import io.symeo.monolithic.backend.domain.port.in.OnboardingFacadeAdapter;
import io.symeo.monolithic.backend.domain.port.out.AccountOnboardingStorage;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OnboardingService implements OnboardingFacadeAdapter {

    private final AccountOnboardingStorage accountOnboardingStorage;

    @Override
    public Onboarding updateOnboarding(Onboarding onboarding) throws SymeoException {
        return accountOnboardingStorage.updateOnboarding(onboarding);
    }
}
