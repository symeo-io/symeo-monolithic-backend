package io.symeo.monolithic.backend.infrastructure.postgres;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Onboarding;
import io.symeo.monolithic.backend.domain.port.out.AccountOnboardingStorage;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OnboardingRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.OnboardingMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.POSTGRES_EXCEPTION;

@AllArgsConstructor
@Slf4j
public class PostgresAccountOnboardingAdapter implements AccountOnboardingStorage {

    private final OnboardingRepository onboardingRepository;

    @Override
    public Onboarding updateOnboarding(Onboarding onboarding) throws SymeoException {
        try {
            return OnboardingMapper.entityToDomain(onboardingRepository.save(OnboardingMapper.domainToEntity(onboarding)));
        } catch (Exception e) {
            LOGGER.error("Failed to update onboarding {}", onboarding);
            throw SymeoException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message("Failed to update onboarding + " + onboarding.getId())
                    .build();
        }
    }
}
