package fr.catlean.monolithic.backend.domain.service;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Onboarding;
import fr.catlean.monolithic.backend.domain.port.in.OnboardingFacadeAdapter;
import fr.catlean.monolithic.backend.domain.port.out.AccountOnboardingStorage;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OnboardingService implements OnboardingFacadeAdapter {

    private final AccountOnboardingStorage accountOnboardingStorage;

    @Override
    public Onboarding updateOnboarding(Onboarding onboarding) throws CatleanException {
        return accountOnboardingStorage.updateOnboarding(onboarding);
    }
}
