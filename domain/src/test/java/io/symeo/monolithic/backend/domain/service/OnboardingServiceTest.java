package io.symeo.monolithic.backend.domain.service;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Onboarding;
import io.symeo.monolithic.backend.domain.port.out.AccountOnboardingStorage;
import io.symeo.monolithic.backend.domain.service.account.OnboardingService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OnboardingServiceTest {

    @Test
    void should_update_onboarding() throws SymeoException {
        // Given
        final AccountOnboardingStorage accountOnboardingStorage = mock(AccountOnboardingStorage.class);
        final OnboardingService onboardingService = new OnboardingService(accountOnboardingStorage);
        final Onboarding onboarding = Onboarding.builder().id(UUID.randomUUID()).hasConnectedToVcs(true).build();

        // When
        final ArgumentCaptor<Onboarding> onboardingArgumentCaptor = ArgumentCaptor.forClass(Onboarding.class);
        when(accountOnboardingStorage.updateOnboarding(onboardingArgumentCaptor.capture())).thenReturn(onboarding);
        onboardingService.updateOnboarding(onboarding);

        // Then
        assertThat(onboardingArgumentCaptor.getValue()).isEqualTo(onboarding);
    }
}