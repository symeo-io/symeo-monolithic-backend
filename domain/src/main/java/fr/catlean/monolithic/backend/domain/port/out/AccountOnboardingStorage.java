package fr.catlean.monolithic.backend.domain.port.out;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Onboarding;

public interface AccountOnboardingStorage {
    Onboarding updateOnboarding(Onboarding onboarding) throws CatleanException;
}
