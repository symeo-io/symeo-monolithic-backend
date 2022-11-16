package io.symeo.monolithic.backend.domain.bff.port.out;

import io.symeo.monolithic.backend.domain.bff.model.account.Onboarding;
import io.symeo.monolithic.backend.domain.exception.SymeoException;

public interface OnboardingStorage {
    Onboarding updateOnboarding(Onboarding onboarding) throws SymeoException;
}
