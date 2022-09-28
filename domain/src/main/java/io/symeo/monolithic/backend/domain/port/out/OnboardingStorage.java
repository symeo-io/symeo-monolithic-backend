package io.symeo.monolithic.backend.domain.port.out;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Onboarding;

public interface OnboardingStorage {
    Onboarding updateOnboarding(Onboarding onboarding) throws SymeoException;
}
