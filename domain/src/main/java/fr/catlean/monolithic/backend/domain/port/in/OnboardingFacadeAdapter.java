package fr.catlean.monolithic.backend.domain.port.in;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Onboarding;

public interface OnboardingFacadeAdapter {
    Onboarding updateOnboarding(Onboarding onboarding) throws CatleanException;
}
