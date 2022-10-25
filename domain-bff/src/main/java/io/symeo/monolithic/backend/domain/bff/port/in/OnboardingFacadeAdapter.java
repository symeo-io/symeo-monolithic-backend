package io.symeo.monolithic.backend.domain.bff.port.in;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.bff.model.account.Onboarding;

public interface OnboardingFacadeAdapter {
    Onboarding updateOnboarding(Onboarding onboarding) throws SymeoException;
}
